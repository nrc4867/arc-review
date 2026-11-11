package dev.chieppa.arc.plugins.manager

import java.nio.file.Files
import kotlin.io.path.createTempDirectory
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PluginPathsTest {
    private val dir = createTempDirectory("arc-plugin-paths")
    private val folder = dir.resolve("todos").also { Files.createDirectories(it.resolve("docs")) }
    private val shared = dir.resolve("shared").also { Files.createDirectories(it) }

    @AfterTest
    fun cleanUp() {
        dir.toFile().deleteRecursively()
    }

    @Test
    fun aPathMustLieUnderAFolderTheManifestNames() {
        val docs = folder.resolve("docs").toRealPath()
        assertEquals(docs.resolve("a.md"), PluginPaths.resolve(folder, listOf("docs"), "docs/a.md"))
        assertEquals(shared.toRealPath().resolve("b.md"), PluginPaths.resolve(folder, listOf("docs", shared.toString()), shared.resolve("b.md").toString()))
        assertNull(PluginPaths.resolve(folder, listOf("docs"), "docs/../plugin.json"))
        assertNull(PluginPaths.resolve(folder, listOf("docs"), shared.resolve("b.md").toString()))
        assertNull(PluginPaths.resolve(folder, emptyList(), "docs/a.md"))
    }
}
