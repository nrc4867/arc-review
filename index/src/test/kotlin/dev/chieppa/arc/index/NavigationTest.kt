package dev.chieppa.arc.index

import dev.chieppa.arc.codeintel.model.Confidence
import dev.chieppa.arc.codeintel.router.Navigation
import dev.chieppa.arc.core.source.RepoName
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class NavigationTest {
    private val api = mapOf(
        "src/main/kotlin/auth/TokenPair.kt" to "package auth\n\nclass TokenPair(val access: String)\n",
        "src/main/kotlin/legacy/TokenPair.kt" to "package legacy\n\nclass TokenPair(val raw: String)\n",
        "src/main/kotlin/auth/TokenService.kt" to """
            package auth

            import java.time.Clock

            class TokenService(private val clock: Clock) {
                fun refresh(token: String): TokenPair {
                    val pair = issue(token, 1)
                    return pair
                }

                private fun issue(token: String, family: Int): TokenPair = TokenPair(token)

                fun log(message: String) = clock.millis()
            }
        """.trimIndent() + "\n",
        "src/main/java/auth/Orders.java" to """
            package auth;

            public final class Orders {
                private static final String PLACE = "{call sales.usp_PlaceOrder(?, ?)}";
                public String place() { return PLACE; }
            }
        """.trimIndent() + "\n",
    )
    private val db = mapOf(
        "procedures/usp_PlaceOrder.sql" to "CREATE OR ALTER PROCEDURE [sales].[usp_PlaceOrder]\n    @CustomerRef NVARCHAR(64),\n    @Total MONEY\nAS\nBEGIN\n    SELECT 1;\nEND\nGO\n",
    )
    private val web = mapOf(
        "src/api/types.ts" to "export interface TokenPair {\n  accessToken: string;\n}\n",
        "src/hooks/useSession.ts" to "import type { TokenPair } from \"../api/types\";\n\nexport function read(raw: unknown): TokenPair {\n  const pair: TokenPair = raw as TokenPair;\n  return pair;\n}\n",
    )

    private fun workspace(dispatcher: kotlinx.coroutines.CoroutineDispatcher): Workspaces {
        val targets = listOf(
            IndexTarget("api#412", RepoName("api"), "a1", setOf("src/main/kotlin/auth/TokenService.kt")),
            IndexTarget("db:main", RepoName("db"), "d1"),
            IndexTarget("web#88", RepoName("web"), "w1"),
        )
        return Workspaces(FakeBlobStore(), dispatcher, targets).apply {
            remember("api", "a1", api)
            remember("db", "d1", db)
            remember("web", "w1", web)
        }
    }

    @Test
    fun aNameInScopeJumpsToTheDefinitionItsPackageSees() = runTest {
        val ws = workspace(StandardTestDispatcher(testScheduler))
        ws.index.update(ws.targets)
        val jump = assertIs<Navigation.Jump>(ws.router.navigate(ws.click("api", "a1", "src/main/kotlin/auth/TokenService.kt", "TokenPair")))
        assertEquals("src/main/kotlin/auth/TokenPair.kt", jump.target.location.document.path)
        assertEquals("api#412", jump.target.source)
        assertEquals("class TokenPair(val access: String)", jump.target.preview)
    }

    @Test
    fun aCallJumpsToTheFunctionInItsOwnFile() = runTest {
        val ws = workspace(StandardTestDispatcher(testScheduler))
        ws.index.update(ws.targets)
        val jump = assertIs<Navigation.Jump>(ws.router.navigate(ws.click("api", "a1", "src/main/kotlin/auth/TokenService.kt", "issue")))
        assertEquals(11, jump.target.location.range.start.line)
    }

    @Test
    fun aLibraryMemberWithNoSourceShowsNothing() = runTest {
        val ws = workspace(StandardTestDispatcher(testScheduler))
        ws.index.update(ws.targets)
        assertEquals(Navigation.Nothing, ws.router.navigate(ws.click("api", "a1", "src/main/kotlin/auth/TokenService.kt", "millis")))
    }

    @Test
    fun aStoredProcedureInAJavaStringLeadsToItsTsqlDefinitionInAnotherRepo() = runTest {
        val ws = workspace(StandardTestDispatcher(testScheduler))
        ws.index.update(ws.targets)
        val jump = assertIs<Navigation.Jump>(ws.router.navigate(ws.click("api", "a1", "src/main/java/auth/Orders.java", "usp_PlaceOrder")))
        assertEquals("db", jump.target.location.document.repo)
        assertEquals("procedures/usp_PlaceOrder.sql", jump.target.location.document.path)
        assertEquals("tsql", jump.target.location.document.language)
    }

    @Test
    fun aClickOnADefinitionListsItsUsagesBySourceWithTheChangedFileFirst() = runTest {
        val ws = workspace(StandardTestDispatcher(testScheduler))
        ws.index.update(ws.targets)
        val usages = assertIs<Navigation.Usages>(ws.router.navigate(ws.click("api", "a1", "src/main/kotlin/auth/TokenPair.kt", "TokenPair"))).answer
        assertEquals(listOf("api#412" to "kotlin", "web#88" to "typescript"), usages.groups.map { it.source to it.language })
        val kotlin = usages.groups.first().usages
        assertEquals(listOf(Confidence.Confident), kotlin.map { it.confidence }.distinct())
        assertEquals(listOf(6, 11, 11), kotlin.map { it.location.range.start.line })
        assertEquals(2, usages.repos)
        assertEquals(setOf("src/api/types.ts", "src/hooks/useSession.ts"), usages.groups[1].usages.map { it.location.document.path }.toSet())
    }

    @Test
    fun theUsagesOfAProcedureIncludeTheStringsThatCallIt() = runTest {
        val ws = workspace(StandardTestDispatcher(testScheduler))
        ws.index.update(ws.targets)
        val usages = assertIs<Navigation.Usages>(ws.router.navigate(ws.click("db", "d1", "procedures/usp_PlaceOrder.sql", "usp_PlaceOrder"))).answer
        val java = usages.groups.single()
        assertEquals("api#412", java.source)
        assertEquals("src/main/java/auth/Orders.java", java.usages.single().location.document.path)
    }

    @Test
    fun movingASourceToANearbyRevisionParsesOnlyTheChangedFiles() = runTest {
        val ws = workspace(StandardTestDispatcher(testScheduler))
        ws.index.update(ws.targets)
        val before = ws.store.reads
        ws.remember("api", "a2", api + ("src/main/kotlin/auth/TokenPair.kt" to "package auth\n\nclass TokenPair(val access: String, val refresh: String)\n"))
        ws.index.update(listOf(ws.targets[0].copy(revision = "a2")) + ws.targets.drop(1))
        assertEquals(1, ws.store.reads - before)
        assertEquals(IndexStatus(sources = 3, files = 7), ws.index.status.value)
    }

    @Test
    fun aFileAtARevisionNoSourceReadsIsParsedOnItsOwn() = runTest {
        val ws = workspace(StandardTestDispatcher(testScheduler))
        ws.index.update(ws.targets)
        ws.remember("api", "old", mapOf("src/main/kotlin/auth/Old.kt" to "package auth\n\nfun old(): TokenPair = TokenPair(\"x\")\n"))
        val jump = assertIs<Navigation.Jump>(ws.router.navigate(ws.click("api", "old", "src/main/kotlin/auth/Old.kt", "TokenPair")))
        assertEquals("src/main/kotlin/auth/TokenPair.kt", jump.target.location.document.path)
    }
}
