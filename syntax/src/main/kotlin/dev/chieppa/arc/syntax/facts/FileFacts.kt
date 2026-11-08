package dev.chieppa.arc.syntax.facts

/**
 * Everything the index keeps about one file's content: its package and imports, definitions in source order and
 * references in source order, its line count, and how many of its [chars] lie under parse errors. It depends only on
 * the content and the grammar, never on the path or revision, so it is shared by every revision with the same blob.
 */
class FileFacts(
    val grammar: String,
    val chars: Int,
    val lineCount: Int,
    val packageName: String?,
    val imports: List<String>,
    val definitions: List<Definition>,
    val references: List<Reference>,
    val errorChars: Int,
) {
    /** The reference whose name covers string index [offset], or null. */
    fun referenceAt(offset: Int): Reference? {
        var low = 0
        var high = references.size - 1
        while (low <= high) {
            val mid = (low + high) ushr 1
            val ref = references[mid]
            when {
                offset < ref.start -> high = mid - 1
                offset >= ref.end -> low = mid + 1
                else -> return ref
            }
        }
        return null
    }

    /** The reference whose name covers [line] (from 1) at [column] (from 0), or null. */
    fun referenceAt(line: Int, column: Int): Reference? {
        var low = 0
        var high = references.size - 1
        while (low <= high) {
            val mid = (low + high) ushr 1
            val ref = references[mid]
            when {
                line < ref.line || (line == ref.line && column < ref.column) -> high = mid - 1
                line > ref.line || column >= ref.column + (ref.end - ref.start) -> low = mid + 1
                else -> return ref
            }
        }
        return null
    }

    /** The definition whose name covers [line] (from 1) at [column] (from 0), or null. */
    fun definitionAt(line: Int, column: Int): Definition? =
        definitions.firstOrNull { it.line == line && column >= it.column && column < it.column + (it.nameEnd - it.nameStart) }

    /** The definition whose name covers string index [offset], or null. */
    fun definitionAt(offset: Int): Definition? = definitions.firstOrNull { offset >= it.nameStart && offset < it.nameEnd }

    /** The innermost definition whose whole span covers [offset], or null. */
    fun enclosing(offset: Int): Definition? =
        definitions.filter { offset >= it.spanStart && offset < it.spanEnd }.minByOrNull { it.spanEnd - it.spanStart }

    companion object {
        /** Facts for a file that could not be read as text. */
        fun empty(grammar: String) = FileFacts(grammar, 0, 0, null, emptyList(), emptyList(), emptyList(), 0)
    }
}
