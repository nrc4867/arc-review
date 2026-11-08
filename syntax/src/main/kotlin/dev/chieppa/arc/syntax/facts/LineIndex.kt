package dev.chieppa.arc.syntax.facts

/** Turns string indices in a text into lines (from 1) and columns (from 0), and back. */
class LineIndex(text: String) {
    private val starts: IntArray = run {
        val list = ArrayList<Int>().apply { add(0) }
        text.forEachIndexed { i, c -> if (c == '\n') list += i + 1 }
        list.toIntArray()
    }

    val lineCount: Int get() = starts.size

    fun line(offset: Int): Int {
        val found = starts.binarySearch(offset)
        return (if (found >= 0) found else -found - 2) + 1
    }

    fun column(offset: Int): Int = offset - starts[line(offset) - 1]

    /** The string index of [line] (from 1), [column] (from 0). */
    fun offset(line: Int, column: Int): Int = starts[(line - 1).coerceIn(0, starts.size - 1)] + column

    /** The string index where [line] (from 1) starts. */
    fun lineStart(line: Int): Int = starts[(line - 1).coerceIn(0, starts.size - 1)]
}
