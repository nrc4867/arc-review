package dev.chieppa.arc.ui.switcher

import java.nio.file.InvalidPathException
import java.nio.file.Path

/** Reads the switcher's field as the folder of a repo to follow: an absolute path, with the quotes a copied path may carry. */
object FolderTarget {
    private const val QUOTES = "\"'"

    /** The folder [text] names, or null when it is not an absolute path. */
    fun read(text: String): Path? {
        val trimmed = text.trim().trim { it in QUOTES }
        if (trimmed.isEmpty()) return null
        val path = try {
            Path.of(trimmed)
        } catch (_: InvalidPathException) {
            return null
        }
        return path.takeIf { it.isAbsolute }
    }
}
