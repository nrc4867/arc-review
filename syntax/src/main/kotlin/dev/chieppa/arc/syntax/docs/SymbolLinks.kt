package dev.chieppa.arc.syntax.docs

/** Builds the Markdown links the parsers write for link tags and references. */
internal object SymbolLinks {
    /**
     * A link to [target] shown as [label]: the URL itself when [target] is one, else a [DocComment.SYMBOL_LINK] link
     * to the target without any parameter list.
     */
    fun render(target: String, label: String): String =
        if (isUrl(target)) "[$label]($target)" else "[$label](${DocComment.SYMBOL_LINK}${target.substringBefore('(')})"

    fun isUrl(text: String): Boolean = text.startsWith("http://") || text.startsWith("https://")

    /** The target and optional label of a link tag's body: split at a bar, else at the first space outside parentheses. */
    fun splitBody(body: String): Pair<String, String?> {
        val trimmed = body.trim()
        val bar = trimmed.indexOf('|')
        if (bar >= 0) return trimmed.substring(0, bar).trim() to trimmed.substring(bar + 1).trim().ifEmpty { null }
        var depth = 0
        for ((index, char) in trimmed.withIndex()) {
            when {
                char == '(' -> depth++
                char == ')' -> depth--
                char.isWhitespace() && depth == 0 ->
                    return trimmed.substring(0, index) to trimmed.substring(index).trim().ifEmpty { null }
            }
        }
        return trimmed to null
    }
}
