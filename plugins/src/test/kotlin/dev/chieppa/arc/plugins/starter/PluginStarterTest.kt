package dev.chieppa.arc.plugins.starter

import dev.chieppa.arc.plugins.manifest.ManifestReader
import dev.chieppa.arc.plugins.manifest.ManifestResult
import dev.chieppa.arc.plugins.source.PluginDiscovery
import dev.chieppa.arc.plugins.config.PluginSourceSpec
import java.nio.file.Files
import kotlin.io.path.createTempDirectory
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class PluginStarterTest {
    private val dir = createTempDirectory("arc-starter")

    @AfterTest
    fun cleanUp() {
        dir.toFile().deleteRecursively()
    }

    @Test
    fun aStarterHoldsAManifestAnEntryItsGuideAndArcsTypesAndReference() {
        val folder = PluginStarter.create(dir, "Todo Counter")
        assertEquals(dir.resolve("todo-counter"), folder)
        val manifest = assertIs<ManifestResult.Read<*>>(ManifestReader.plugin(Files.readString(folder.resolve(StarterFiles.MANIFEST)))).value
        assertEquals(PluginDiscovery.find(PluginSourceSpec.Directory(folder.toString())).single().manifest, manifest)
        val entry = Files.readString(folder.resolve(StarterFiles.ENTRY))
        assertTrue("label: \"Todo Counter: say hello\"" in entry, entry)
        assertTrue("id: \"${StarterFiles.PANEL}\"" in entry && "id: \"${StarterFiles.ACTION}\"" in entry)
        assertFalse("{{" in entry || "{{" in Files.readString(folder.resolve(StarterFiles.GUIDE)))
        listOf("arc/types/generated/arc.d.ts", "arc/types/sdk/index.ts", "arc/types/shims/react.d.ts", "arc/reference.md", StarterFiles.TSCONFIG).forEach {
            assertTrue(Files.isRegularFile(folder.resolve(it)), it)
        }
        assertTrue("setSetting<K" in Files.readString(folder.resolve("arc/types/generated/arc.d.ts")))
    }

    @Test
    fun aFolderHoldingFilesIsLeftAlone() {
        Files.createDirectories(dir.resolve("taken"))
        Files.writeString(dir.resolve("taken/notes.txt"), "mine")
        val refused = assertFailsWith<StarterException> { PluginStarter.create(dir, "Taken") }
        assertEquals(StarterProblem.FolderTaken, refused.problem)
        assertEquals(listOf("notes.txt"), Files.list(dir.resolve("taken")).use { files -> files.map { it.fileName.toString() }.toList() })
        assertEquals(StarterProblem.BadName, assertFailsWith<StarterException> { PluginStarter.create(dir, "\"") }.problem)
    }
}
