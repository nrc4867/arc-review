package dev.chieppa.arc.plugins.runtime

import dev.chieppa.arc.plugins.manifest.Capabilities
import dev.chieppa.arc.plugins.manifest.FileAccess
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals

class PermissionFlagsTest {
    private val folder = Path.of("plugins", "todos").toAbsolutePath()
    private val host = Path.of("runtime", "host.js").toAbsolutePath()

    @Test
    fun grantsOnlyThePluginsOwnFolderAndTheHostWhenNothingIsDeclared() {
        assertEquals(listOf("--permission", "--allow-fs-read=$folder", "--allow-fs-read=$host"), PermissionFlags.of(Capabilities(), folder, listOf(host), 26))
    }

    @Test
    fun grantsWhatTheManifestDeclares() {
        val capabilities = Capabilities(files = FileAccess(read = listOf("../shared"), write = listOf("out")), network = listOf("api.github.com"), processes = true)
        val flags = PermissionFlags.of(capabilities, folder, listOf(host), 26)
        assertEquals(
            listOf(
                "--permission",
                "--allow-fs-read=$folder",
                "--allow-fs-read=$host",
                "--allow-fs-read=${folder.resolve("../shared").normalize()}",
                "--allow-fs-write=${folder.resolve("out")}",
                "--allow-child-process",
                "--allow-net",
            ),
            flags,
        )
        assertEquals(false, "--allow-net" in PermissionFlags.of(capabilities, folder, listOf(host), 24))
    }
}
