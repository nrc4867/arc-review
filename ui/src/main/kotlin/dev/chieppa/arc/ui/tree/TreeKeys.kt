package dev.chieppa.arc.ui.tree

import dev.chieppa.arc.core.source.Source

/**
 * The keys the file tree knows its rows by. Each holds its source's ref, so the rows open under one source can be
 * kept with that source's view.
 */
object TreeKeys {
    private const val SEPARATOR = "|"
    private const val SOURCE = "s"
    private const val FOLDER = "d"
    private const val FILE = "f"
    private const val ASIDE = "a"

    fun source(source: Source): String = SOURCE + SEPARATOR + source.ref

    fun folder(source: Source, path: String): String = listOf(FOLDER, source.ref, path).joinToString(SEPARATOR)

    fun file(source: Source, path: String): String = listOf(FILE, source.ref, path).joinToString(SEPARATOR)

    fun aside(source: Source): String = ASIDE + SEPARATOR + source.ref

    /** Whether [key] is a row under [source], or [source]'s own row. */
    fun belongsTo(key: String, source: Source): Boolean = key.split(SEPARATOR).getOrNull(1) == source.ref

    /** The source ref and path a file row's [key] stands for, or null for any other row. */
    fun fileOf(key: Any): Pair<String, String>? {
        val text = key as? String ?: return null
        val parts = text.split(SEPARATOR, limit = FILE_PARTS)
        return if (parts.size == FILE_PARTS && parts[0] == FILE) parts[1] to parts[2] else null
    }

    private const val FILE_PARTS = 3
}
