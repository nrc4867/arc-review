package dev.chieppa.arc.plugins.console

/** Output kept by lines: pieces cut at line breaks, and the pieces that make up the last lines. */
object ConsoleLines {
    private const val BREAK = '\n'

    /** [chunk] cut after each line break: every piece but the last ends one, and the last may be a partial line. */
    fun split(chunk: ConsoleChunk): List<ConsoleChunk> {
        val pieces = mutableListOf<ConsoleChunk>()
        var start = 0
        while (start < chunk.text.length) {
            val end = chunk.text.indexOf(BREAK, start).let { if (it < 0) chunk.text.length else it + 1 }
            pieces += chunk.copy(text = chunk.text.substring(start, end))
            start = end
        }
        return pieces
    }

    /**
     * The pieces of [chunks], each holding at most one line, that make up its last [max] lines, a partial last line
     * counted as one.
     */
    fun last(chunks: List<ConsoleChunk>, max: Int): List<ConsoleChunk> {
        if (chunks.isEmpty() || max <= 0) return emptyList()
        var lines = 1
        var start = chunks.lastIndex
        for (index in chunks.lastIndex - 1 downTo 0) {
            if (ends(chunks[index])) lines++
            if (lines > max) break
            start = index
        }
        return if (start == 0) chunks else chunks.subList(start, chunks.size).toList()
    }

    /** Whether [chunk] ends its line. */
    fun ends(chunk: ConsoleChunk): Boolean = chunk.text.lastOrNull() == BREAK
}
