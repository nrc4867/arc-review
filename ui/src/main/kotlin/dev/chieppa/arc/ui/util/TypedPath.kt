package dev.chieppa.arc.ui.util

import java.io.File
import java.nio.file.InvalidPathException
import java.nio.file.Path

/**
 * Reads a path a person typed or pasted: trimmed, without the quotes a copied path carries, with `~`, `~/…` and on
 * Windows `~\…` naming the home folder. Any other relative path is no path, since the app's working folder means
 * nothing to the person (it is `/` when Arc starts from its macOS launcher).
 */
object TypedPath {
    private const val QUOTES = "\"'"
    private const val HOME = "~"

    /** The person's home folder. */
    val userHome: Path get() = Path.of(System.getProperty("user.home"))

    /**
     * The absolute path [text] names, `~` read as [home]; null when it is blank, relative or not a path. [windows] also
     * takes `~\` before the rest.
     */
    fun read(text: String, home: Path = userHome, windows: Boolean = File.separatorChar == '\\'): Path? {
        val typed = text.trim().trim { it in QUOTES }.trim()
        if (typed.isEmpty()) return null
        val path = try {
            when {
                typed == HOME -> home
                typed.startsWith("$HOME/") || (windows && typed.startsWith("$HOME\\")) -> home.resolve(typed.substring(2))
                else -> Path.of(typed)
            }
        } catch (_: InvalidPathException) {
            return null
        }
        return path.takeIf { it.isAbsolute }
    }
}
