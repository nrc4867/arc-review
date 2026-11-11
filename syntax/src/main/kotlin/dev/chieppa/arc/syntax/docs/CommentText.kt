package dev.chieppa.arc.syntax.docs

/** Cuts the delimiters and leading stars off a block comment. */
internal object CommentText {
    /**
     * The lines of the block comment [raw] without its delimiters, and without the star (and one space after it)
     * that starts each line; indentation beyond that is kept. Line `i` is on file line `firstLine + i`.
     */
    fun blockLines(raw: String): List<String> {
        val opener = if (raw.startsWith(DocSyntax.DOC_OPEN)) DocSyntax.DOC_OPEN else DocSyntax.BLOCK_OPEN
        val body = raw.removePrefix(opener).removeSuffix(DocSyntax.BLOCK_CLOSE)
        return body.split('\n').mapIndexed { index, line ->
            val clean = line.trimEnd('\r', ' ', '\t')
            if (index == 0) clean.trim() else stripStar(clean)
        }
    }

    private fun stripStar(line: String): String {
        val trimmed = line.trimStart()
        if (!trimmed.startsWith("*")) return trimmed
        val rest = trimmed.substring(1)
        return if (rest.startsWith(" ")) rest.substring(1) else rest
    }

    /** [lines] joined, without blank lines at either end. */
    fun join(lines: List<String>): String = lines.joinToString("\n").trim('\n', '\r').trimEnd()
}
