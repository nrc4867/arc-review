package dev.chieppa.arc.syntax.docs

/** A comment found in a file: its [text] with delimiters, the index it [start]s at and the line it starts on. */
internal data class RawComment(val text: String, val start: Int, val firstLine: Int)

/** Finds the comment written right before a definition. */
internal object DocCommentLocator {
    /** The doc block comment ending right before [offset], after skipping JavaScript modifier keywords when [skipKeywords]. */
    fun docBlock(text: String, offset: Int, skipKeywords: Boolean): RawComment? {
        var end = skipSpaceBack(text, offset)
        if (skipKeywords) end = skipKeywordsBack(text, end)
        val block = blockEndingAt(text, end) ?: return null
        return block.takeIf { it.text.startsWith(DocSyntax.DOC_OPEN) && it.text.length >= DocSyntax.DOC_OPEN.length + DocSyntax.BLOCK_CLOSE.length }
    }

    /** A block comment ending right before [offset], else the run of line comments directly above it. */
    fun sqlComment(text: String, offset: Int): RawComment? {
        val end = skipSpaceBack(text, offset)
        blockEndingAt(text, end)?.let { return it }
        if (text.substring(end, offset).count { it == '\n' } > 1) return null
        return lineRun(text, end)
    }

    private fun lineRun(text: String, end: Int): RawComment? {
        var start = -1
        var lineEnd = end
        while (lineEnd > 0) {
            val lineStart = text.lastIndexOf('\n', lineEnd - 1) + 1
            val line = text.substring(lineStart, lineEnd)
            val indent = line.length - line.trimStart().length
            if (!line.trimStart().startsWith(DocSyntax.SQL_LINE)) break
            start = lineStart + indent
            lineEnd = lineStart - 1
            if (lineEnd > 0 && text[lineEnd - 1] == '\r') lineEnd--
        }
        return if (start < 0) null else RawComment(text.substring(start, end), start, lineAt(text, start))
    }

    private fun blockEndingAt(text: String, end: Int): RawComment? {
        if (end < DocSyntax.BLOCK_OPEN.length + DocSyntax.BLOCK_CLOSE.length || !text.startsWith(DocSyntax.BLOCK_CLOSE, end - 2)) return null
        val previousClose = text.lastIndexOf(DocSyntax.BLOCK_CLOSE, end - 3)
        val start = text.indexOf(DocSyntax.BLOCK_OPEN, if (previousClose < 0) 0 else previousClose + 2)
        if (start < 0 || start + DocSyntax.BLOCK_OPEN.length > end - 2) return null
        return RawComment(text.substring(start, end), start, lineAt(text, start))
    }

    private fun skipSpaceBack(text: String, from: Int): Int {
        var index = from
        while (index > 0 && text[index - 1].isWhitespace()) index--
        return index
    }

    private fun skipKeywordsBack(text: String, from: Int): Int {
        var end = from
        while (true) {
            var start = end
            while (start > 0 && (text[start - 1].isLetterOrDigit() || text[start - 1] == '_' || text[start - 1] == '$')) start--
            if (start == end || text.substring(start, end) !in DocSyntax.javaScriptKeywords) return end
            end = skipSpaceBack(text, start)
        }
    }

    /** The 1-based line of [index] in [text]. */
    fun lineAt(text: String, index: Int): Int = 1 + (0 until index).count { text[it] == '\n' }
}
