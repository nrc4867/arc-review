package dev.chieppa.arc.index

import dev.chieppa.arc.codeintel.model.ImplementationsRequest
import dev.chieppa.arc.codeintel.router.Navigation
import dev.chieppa.arc.core.source.RepoName
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ImplementationsTest {
    private val kotlin = mapOf(
        "src/store/Store.kt" to "package store\n\ninterface Store {\n    fun load(key: String): String\n}\n",
        "src/store/Cached.kt" to "package store\n\nabstract class Cached : Store {\n    override fun load(key: String): String = key\n}\n",
        "src/store/Disk.kt" to "package store\n\nclass Disk : Cached() {\n    override fun load(key: String): String = \"disk\"\n}\n",
        "src/store/Memory.kt" to "package store\n\nobject Memory : Store {\n    override fun load(key: String) = \"memory\"\n}\n",
        "src/store/Use.kt" to "package store\n\nfun read(store: Store) = store.load(\"a\")\n",
    )
    private val java = mapOf(
        "src/main/java/shop/Payment.java" to "package shop;\n\npublic interface Payment {\n    void pay(int cents);\n}\n",
        "src/main/java/shop/Card.java" to "package shop;\n\npublic class Card implements Payment {\n    public void pay(int cents) {}\n}\n",
        "src/main/java/shop/Gift.java" to "package shop;\n\npublic final class Gift extends Card {\n    @Override public void pay(int cents) {}\n}\n",
    )
    private val web = mapOf(
        "src/shape.ts" to "export interface Shape {\n  area(): number;\n}\n",
        "src/square.ts" to "import { Shape } from \"./shape\";\n\nexport class Square implements Shape {\n  area(): number { return 1; }\n}\n",
        "src/circle.tsx" to "import { Shape } from \"./shape\";\n\nexport abstract class Round implements Shape {\n  abstract area(): number;\n}\nexport class Circle extends Round {\n  area(): number { return 3; }\n}\n",
    )

    private fun workspace(dispatcher: CoroutineDispatcher): Workspaces {
        val targets = listOf(IndexTarget("api#1", RepoName("api"), "a1"), IndexTarget("web#2", RepoName("web"), "w1"))
        return Workspaces(FakeBlobStore(), dispatcher, targets).apply {
            remember("api", "a1", kotlin + java)
            remember("web", "w1", web)
        }
    }

    private suspend fun Workspaces.implementations(repo: String, revision: String, path: String, name: String): List<String> {
        val click = click(repo, revision, path, name)
        val answer = router.implementations(ImplementationsRequest(click.document, click.at, click.sources))
        return answer?.targets.orEmpty().map { "${it.location.document.path}:${it.location.range.start.line}" }.sorted()
    }

    @Test
    fun aKotlinInterfaceListsEveryTypeUnderItAndItsMethodTheOverrides() = runTest {
        val ws = workspace(StandardTestDispatcher(testScheduler))
        ws.index.update(ws.targets)
        assertEquals(
            listOf("src/store/Cached.kt:3", "src/store/Disk.kt:3", "src/store/Memory.kt:3"),
            ws.implementations("api", "a1", "src/store/Store.kt", "Store"),
        )
        assertEquals(
            listOf("src/store/Cached.kt:4", "src/store/Disk.kt:4", "src/store/Memory.kt:4"),
            ws.implementations("api", "a1", "src/store/Store.kt", "load"),
        )
        // A use of the type leads to the same types.
        assertEquals(3, ws.implementations("api", "a1", "src/store/Use.kt", "Store").size)
    }

    @Test
    fun aJavaInterfaceListsItsImplementationsAndOverrides() = runTest {
        val ws = workspace(StandardTestDispatcher(testScheduler))
        ws.index.update(ws.targets)
        assertEquals(
            listOf("src/main/java/shop/Card.java:3", "src/main/java/shop/Gift.java:3"),
            ws.implementations("api", "a1", "src/main/java/shop/Payment.java", "Payment"),
        )
        assertEquals(
            listOf("src/main/java/shop/Card.java:4", "src/main/java/shop/Gift.java:4"),
            ws.implementations("api", "a1", "src/main/java/shop/Payment.java", "pay"),
        )
    }

    @Test
    fun aTypeScriptInterfaceListsItsImplementationsAcrossTsAndTsx() = runTest {
        val ws = workspace(StandardTestDispatcher(testScheduler))
        ws.index.update(ws.targets)
        assertEquals(
            listOf("src/circle.tsx:3", "src/circle.tsx:6", "src/square.ts:3"),
            ws.implementations("web", "w1", "src/shape.ts", "Shape"),
        )
        assertEquals(
            listOf("src/circle.tsx:4", "src/circle.tsx:7", "src/square.ts:4"),
            ws.implementations("web", "w1", "src/shape.ts", "area"),
        )
    }

    @Test
    fun aCtrlClickOnAnInterfaceMethodWithNoUsagesOffersItsImplementations() = runTest {
        val ws = workspace(StandardTestDispatcher(testScheduler))
        ws.index.update(ws.targets)
        val choose = assertIs<Navigation.Choose>(ws.router.navigate(ws.click("api", "a1", "src/main/java/shop/Payment.java", "pay")))
        assertEquals(2, choose.answer.targets.size)
    }
}
