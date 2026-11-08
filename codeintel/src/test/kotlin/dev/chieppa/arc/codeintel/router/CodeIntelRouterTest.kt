package dev.chieppa.arc.codeintel.router

import dev.chieppa.arc.codeintel.Answers
import dev.chieppa.arc.codeintel.CodeIntel
import dev.chieppa.arc.codeintel.model.CodePosition
import dev.chieppa.arc.codeintel.model.Confidence
import dev.chieppa.arc.codeintel.model.DefinitionAnswer
import dev.chieppa.arc.codeintel.model.DefinitionRequest
import dev.chieppa.arc.codeintel.model.DocumentSelector
import dev.chieppa.arc.codeintel.model.SourceScope
import dev.chieppa.arc.codeintel.model.UsageGroup
import dev.chieppa.arc.codeintel.model.UsagesAnswer
import dev.chieppa.arc.codeintel.model.UsagesRequest
import dev.chieppa.arc.codeintel.provider.DefinitionProvider
import dev.chieppa.arc.codeintel.provider.UsagesProvider
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class CodeIntelRouterTest {
    private val sources = listOf(SourceScope("api#412", "api", "9c1e2fa"), SourceScope("web#88", "web", "e41b07d"))
    private val request = DefinitionRequest(Answers.document("a/Click.kt"), CodePosition(3, 8), sources)

    private class Definitions(override val id: String, override val rank: Int = 0, val answer: () -> DefinitionAnswer?) : DefinitionProvider {
        override val selector = DocumentSelector(setOf("kotlin"))
        override suspend fun definitions(request: DefinitionRequest) = answer()
    }

    private class Usages(override val id: String, val answer: UsagesAnswer) : UsagesProvider {
        override val selector = DocumentSelector.All
        override suspend fun usages(request: UsagesRequest) = answer
    }

    @Test
    fun oneConfidentDefinitionJumps() = runTest {
        val intel = CodeIntel()
        intel.definitions.register(Definitions("ts") { DefinitionAnswer("TokenPair", listOf(Answers.target("a/T.kt", 3), Answers.target("b/T.kt", 9, Confidence.Filtered))) })
        val navigation = CodeIntelRouter(intel).navigate(request)
        assertEquals("a/T.kt", assertIs<Navigation.Jump>(navigation).target.location.document.path)
    }

    @Test
    fun severalOrOnlyFilteredCandidatesAreListedConfidentFirst() = runTest {
        val intel = CodeIntel()
        intel.definitions.register(Definitions("ts") { DefinitionAnswer("TokenPair", listOf(Answers.target("b/T.kt", 9, Confidence.Filtered))) })
        val choose = assertIs<Navigation.Choose>(CodeIntelRouter(intel).navigate(request))
        assertEquals(listOf(Confidence.Filtered), choose.answer.targets.map { it.confidence })
    }

    @Test
    fun noCandidatesShowNothingAndAFailingProviderIsLeftOut() = runTest {
        val intel = CodeIntel()
        intel.definitions.register(Definitions("broken") { error("plugin went away") })
        intel.definitions.register(Definitions("ts") { DefinitionAnswer("println", emptyList()) })
        assertEquals(Navigation.Nothing, CodeIntelRouter(intel).navigate(request))
    }

    @Test
    fun anExactAnswerSupersedesByNameAnswersInItsLanguage() = runTest {
        val intel = CodeIntel()
        intel.definitions.register(Definitions("ts") { DefinitionAnswer("TokenPair", listOf(Answers.target("a/T.kt", 3), Answers.target("b/T.kt", 9))) })
        intel.definitions.register(Definitions("lsp", rank = 1) { DefinitionAnswer("TokenPair", listOf(Answers.target("b/T.kt", 9, origin = Answers.exact))) })
        val answer = CodeIntelRouter(intel).definitions(request)!!
        assertEquals(listOf("b/T.kt" to Confidence.Confident, "a/T.kt" to Confidence.Filtered), answer.targets.map { it.location.document.path to it.confidence })
        assertEquals(Answers.exact, answer.targets.first().origin)
    }

    @Test
    fun aClickOnADefinitionListsItsUsagesBySourceOrder() = runTest {
        val intel = CodeIntel()
        intel.definitions.register(Definitions("ts") { DefinitionAnswer("TokenPair", emptyList(), atDefinition = true) })
        val web = UsageGroup("web#88", "e41b07d", "typescript", Answers.byName, listOf(Answers.target("src/types.ts", 12)))
        val api = UsageGroup("api#412", "9c1e2fa", "kotlin", Answers.byName, listOf(Answers.target("a/T.kt", 47)))
        intel.usages.register(Usages("ts", UsagesAnswer("TokenPair", listOf(web, api))))
        val usages = assertIs<Navigation.Usages>(CodeIntelRouter(intel).navigate(request))
        assertEquals(listOf("api#412", "web#88"), usages.answer.groups.map { it.source })
        assertEquals(2, usages.answer.count)
    }

    @Test
    fun aProviderTakesOnlyTheFilesItsSelectorTakes() = runTest {
        val intel = CodeIntel()
        val registration = intel.definitions.register(Definitions("ts") { DefinitionAnswer("x", listOf(Answers.target("a/T.kt", 3))) })
        val sql = request.copy(document = Answers.document("db/x.sql", "tsql"))
        assertEquals(Navigation.Nothing, CodeIntelRouter(intel).navigate(sql))
        assertIs<Navigation.Jump>(CodeIntelRouter(intel).navigate(request))
        registration.close()
        assertEquals(Navigation.Nothing, CodeIntelRouter(intel).navigate(request))
    }

    @Test
    fun usagesListChangedFilesFirstThenConfidentThenPlace() {
        val ordered = AnswerMerging.orderUsages(
            listOf(Answers.target("z/Z.kt", 1), Answers.target("b/B.kt", 5, Confidence.Filtered), Answers.target("b/B.kt", 2), Answers.target("a/A.kt", 9)),
            changedPaths = listOf("z/Z.kt"),
        )
        assertEquals(listOf("z/Z.kt:1", "a/A.kt:9", "b/B.kt:2", "b/B.kt:5"), ordered.map { "${it.location.document.path}:${it.location.range.start.line}" })
    }
}
