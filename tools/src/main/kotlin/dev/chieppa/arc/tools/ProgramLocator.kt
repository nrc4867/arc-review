package dev.chieppa.arc.tools

import java.io.File
import java.nio.file.Files
import java.nio.file.Path

/**
 * Finds a program the person installed: on the `PATH` of [env], then in the folders package managers install into,
 * since an app started from the Dock or Finder gets the system's short `PATH` (`/usr/bin:/bin:/usr/sbin:/sbin`) rather
 * than the one the person's shell sets up.
 */
class ProgramLocator(
    private val env: Map<String, String> = System.getenv(),
    private val home: String = System.getProperty("user.home"),
    private val windows: Boolean = HostPlatform.isWindows,
) {
    /** The full path of [name] where it is found; [name] itself otherwise, for the system to look up. */
    fun find(name: String): String = folders().asSequence()
        .flatMap { folder -> candidates(name).map { Path.of(folder, it) } }
        .firstOrNull { Files.isRegularFile(it) && (windows || Files.isExecutable(it)) }
        ?.toString() ?: name

    private fun folders(): List<String> {
        val path = env[PATH] ?: env.entries.firstOrNull { it.key.equals(PATH, ignoreCase = true) }?.value.orEmpty()
        val listed = path.split(File.pathSeparatorChar).filter { it.isNotBlank() }
        val known = if (windows) emptyList() else UNIX_FOLDERS + listOf("$home/.local/bin", "$home/bin")
        return (listed + known).distinct()
    }

    private fun candidates(name: String): List<String> =
        if (windows) (env[PATHEXT] ?: DEFAULT_PATHEXT).split(';').filter { it.isNotBlank() }.map { name + it.lowercase() } + name else listOf(name)

    companion object {
        private const val PATH = "PATH"
        private const val PATHEXT = "PATHEXT"
        private const val DEFAULT_PATHEXT = ".COM;.EXE;.BAT;.CMD"

        /** Where Homebrew (Apple silicon and Intel), MacPorts and Nix put programs. */
        val UNIX_FOLDERS = listOf("/opt/homebrew/bin", "/usr/local/bin", "/opt/local/bin", "/nix/var/nix/profiles/default/bin", "/run/current-system/sw/bin")
    }
}
