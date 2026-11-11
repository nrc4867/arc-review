package dev.chieppa.arc.plugins.runtime

import kotlinx.coroutines.test.runTest
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createTempDirectory
import kotlin.io.path.readText
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ToolInstallerTest {
    private val dir = createTempDirectory("arc-tools")
    private val fetched = mutableListOf<String>()

    @AfterTest
    fun cleanUp() {
        dir.toFile().deleteRecursively()
    }

    /** "Downloads" by copying the archive the url names from [archives]. */
    private fun downloader(archives: Map<String, Path>) = Downloader { url, _, target ->
        fetched += url
        Files.copy(archives[url] ?: throw IOException("404"), target)
    }

    @Test
    fun installsATarGzOnceAndUsesItAfter() = runTest {
        val archive = dir.resolve("node.tar.gz")
        TestArchives.tarGz(
            archive,
            mapOf("node-v1/" to "", "node-v1/bin/node" to "#!node", "node-v1/README" to "read me"),
            executable = setOf("node-v1/bin/node"),
            links = mapOf("node-v1/bin/npm" to "../lib/npm"),
        )
        val pin = ToolPin("node", "1.0.0", "https://x/node.tar.gz", "sha", "node-v1/bin/node")
        val installer = ToolInstaller(dir.resolve("tools"), downloader(mapOf(pin.url to archive)))
        assertFalse(installer.installed(pin))
        val program = installer.install(pin)
        assertEquals("#!node", program.readText())
        assertEquals(dir.resolve("tools/node-1.0.0/node-v1/README").readText(), "read me")
        assertFalse(Files.exists(dir.resolve("tools/node-1.0.0/node-v1/bin/npm")))
        assertEquals(program, installer.install(pin))
        assertEquals(1, fetched.size)
        assertTrue(installer.installed(pin))
    }

    @Test
    fun installsAZip() = runTest {
        val archive = dir.resolve("node.zip")
        TestArchives.zip(archive, mapOf("node-v1/node.exe" to "exe"))
        val pin = ToolPin("node", "1.0.0", "https://x/node.zip", "sha", "node-v1/node.exe")
        assertEquals("exe", ToolInstaller(dir.resolve("tools"), downloader(mapOf(pin.url to archive))).install(pin).readText())
    }

    @Test
    fun aFailedOrWrongArchiveLeavesNothingInstalled() = runTest {
        val archive = dir.resolve("other.zip")
        TestArchives.zip(archive, mapOf("other/file" to "x"))
        val pin = ToolPin("node", "1.0.0", "https://x/other.zip", "sha", "node-v1/node.exe")
        val installer = ToolInstaller(dir.resolve("tools"), downloader(mapOf(pin.url to archive)))
        assertFailsWith<IOException> { installer.install(pin) }
        assertFailsWith<IOException> { installer.install(pin.copy(url = "https://x/missing.zip")) }
        assertEquals(emptyList(), Files.list(dir.resolve("tools")).use { it.toList() })
    }

    @Test
    fun anArchiveCannotWriteOutsideItsFolder() {
        val archive = dir.resolve("evil.zip")
        TestArchives.zip(archive, mapOf("../escaped" to "x"))
        assertFailsWith<IOException> { ArchiveUnpacker.unpack(archive, zip = true, into = dir.resolve("into")) }
        assertFalse(Files.exists(dir.resolve("escaped")))
    }

    @Test
    fun theBundledPinsCoverThisHost() {
        val pins = ToolPins.bundled()
        val node = assertNotNull(pins.pin(ToolPins.NODE))
        assertEquals(64, node.sha256.length)
        assertEquals(26, PluginTools.major(node.version))
        assertEquals(24, PluginTools.major("v24.16.0"))
    }
}
