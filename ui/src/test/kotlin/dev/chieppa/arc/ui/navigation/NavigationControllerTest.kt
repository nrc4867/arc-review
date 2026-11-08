package dev.chieppa.arc.ui.navigation

import dev.chieppa.arc.codeintel.model.AnswerOrigin
import dev.chieppa.arc.codeintel.model.CodeLocation
import dev.chieppa.arc.codeintel.model.CodeRange
import dev.chieppa.arc.codeintel.model.Confidence
import dev.chieppa.arc.codeintel.model.DecorationStyle
import dev.chieppa.arc.codeintel.model.DefinitionAnswer
import dev.chieppa.arc.codeintel.model.DefinitionRequest
import dev.chieppa.arc.codeintel.model.DocumentRef
import dev.chieppa.arc.codeintel.model.DocumentSelector
import dev.chieppa.arc.codeintel.model.Precision
import dev.chieppa.arc.codeintel.model.SymbolTarget
import dev.chieppa.arc.codeintel.model.UsageGroup
import dev.chieppa.arc.codeintel.model.UsagesAnswer
import dev.chieppa.arc.codeintel.model.UsagesRequest
import dev.chieppa.arc.codeintel.provider.DefinitionProvider
import dev.chieppa.arc.codeintel.provider.UsagesProvider
import dev.chieppa.arc.codeintel.router.Navigation
import dev.chieppa.arc.core.pane.PaneView
import dev.chieppa.arc.core.source.RepoName
import dev.chieppa.arc.ui.review.PaneController
import dev.chieppa.arc.ui.review.WorkspaceState
import dev.chieppa.arc.ui.util.ArcDispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class NavigationControllerTest {
    private val origin = AnswerOrigin("test", Precision.ByName, "kotlin")
    private val clicked = DocumentRef("api", "a1", "src/Service.kt", "kotlin")
    private val name = NameSpan(3, 8, 17)

    private fun target(path: String, line: Int, confidence: Confidence = Confidence.Confident) =
        SymbolTarget(CodeLocation(DocumentRef("api", "a1", path, "kotlin"), CodeRange.onLine(line, 6, 9)), "TokenPair", "class", "class TokenPair", confidence, origin)

    private class Definitions(val answer: DefinitionAnswer) : DefinitionProvider {
        override val id = "test"
        override val selector = DocumentSelector.All
        override suspend fun definitions(request: DefinitionRequest) = answer
    }

    private class Usages(val answer: UsagesAnswer) : UsagesProvider {
        override val id = "test"
        override val selector = DocumentSelector.All
        override suspend fun usages(request: UsagesRequest) = answer
    }

    private fun TestScope.controller(state: WorkspaceState, navigation: CodeNavigation): NavigationController {
        val dispatcher = StandardTestDispatcher(testScheduler)
        return NavigationController(state, navigation, SymbolOpener(state, PaneController(state)), this, ArcDispatchers.on(dispatcher))
    }

    @Test
    fun oneConfidentDefinitionOpensItsFileWholeAndMarksBothNames() = runTest {
        val navigation = CodeNavigation()
        navigation.intel.definitions.register(Definitions(DefinitionAnswer("TokenPair", listOf(target("src/TokenPair.kt", 5)))))
        val state = WorkspaceState()
        val controller = controller(state, navigation)
        controller.click("p1", clicked, name, null)
        testScheduler.advanceUntilIdle()
        val pane = state.workspace.panes.single()
        assertEquals("src/TokenPair.kt", pane.path)
        assertEquals(PaneView.FullFile, pane.view)
        assertEquals(NavReveal(RepoName("api"), "a1", "src/TokenPair.kt", 5, 6), controller.reveal)
        assertEquals(listOf(DecorationStyle.Reference, DecorationStyle.Definition), controller.focus.map { it.style })
        assertNull(controller.popup)
    }

    @Test
    fun severalCandidatesOpenAListUnderTheName() = runTest {
        val navigation = CodeNavigation()
        val answer = DefinitionAnswer("TokenPair", listOf(target("a/T.kt", 1), target("b/T.kt", 2)))
        navigation.intel.definitions.register(Definitions(answer))
        val state = WorkspaceState()
        val controller = controller(state, navigation)
        controller.click("p1", clicked, name, null)
        testScheduler.advanceUntilIdle()
        val popup = controller.popup!!
        assertEquals("p1", popup.paneId)
        assertEquals(answer, assertIs<Navigation.Choose>(popup.navigation).answer)
        controller.open(answer.targets[1])
        assertNull(controller.popup)
        assertEquals("b/T.kt", state.workspace.panes.single().path)
    }

    @Test
    fun showUsagesListsTheUsagesOfTheDefinitionANameLeadsTo() = runTest {
        val navigation = CodeNavigation()
        navigation.intel.definitions.register(Definitions(DefinitionAnswer("TokenPair", listOf(target("src/TokenPair.kt", 5)))))
        val usages = UsagesAnswer("TokenPair", listOf(UsageGroup("api#412", "a1", "kotlin", origin, listOf(target("src/Service.kt", 3)))))
        navigation.intel.usages.register(Usages(usages))
        val state = WorkspaceState()
        val controller = controller(state, navigation)
        controller.askUsagesNext()
        controller.click("p1", clicked, name, null)
        testScheduler.advanceUntilIdle()
        assertEquals(usages, assertIs<Navigation.Usages>(controller.popup!!.navigation).answer)
        assertEquals(listOf(DecorationStyle.Reference), controller.focus.map { it.style })
        assertEquals(emptyList(), state.workspace.panes)
    }

    @Test
    fun aNameWithNoDefinitionHereDoesNothing() = runTest {
        val state = WorkspaceState()
        val controller = controller(state, CodeNavigation())
        controller.click("p1", clicked, name, null)
        testScheduler.advanceUntilIdle()
        assertNull(controller.popup)
        assertEquals(emptyList(), state.workspace.panes)
    }
}
