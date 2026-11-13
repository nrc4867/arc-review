package dev.chieppa.arc.plugins.runtime

import dev.chieppa.arc.tools.HostPlatform
import java.nio.file.Path

/**
 * The environment the plugins' Node process gets: only what Node needs on the host, taken from Arc's [of] `parent`,
 * with `PATH` naming only Node's own folder. Nothing else of Arc's, such as its GitHub token, reaches a plugin.
 */
object NodeEnvironment {
    /** What Node needs on Windows: where the system is, and where temporary files go. */
    val WINDOWS: List<String> = listOf("SystemRoot", "SystemDrive", "windir", "TEMP", "TMP")

    /** What Node needs on macOS and Linux: where temporary files go, and the language of its messages. */
    val UNIX: List<String> = listOf("TMPDIR", "LANG")

    private const val PATH = "PATH"

    fun of(parent: Map<String, String>, nodeFolder: Path, windows: Boolean = HostPlatform.isWindows): Map<String, String> = buildMap {
        for (name in if (windows) WINDOWS else UNIX) {
            parent.entries.firstOrNull { it.key.equals(name, ignoreCase = windows) }?.let { put(name, it.value) }
        }
        put(PATH, nodeFolder.toAbsolutePath().toString())
    }
}
