package dev.chieppa.arc.syntax.docs

/** Small text cuts the tag parsers share. */
internal object TextSplit {
    /** The first whitespace-delimited word of [text] and the rest, both trimmed. */
    fun firstWord(text: String): Pair<String, String> {
        val trimmed = text.trim()
        val end = trimmed.indexOfFirst { it.isWhitespace() }
        return if (end < 0) trimmed to "" else trimmed.substring(0, end) to trimmed.substring(end).trim()
    }

    /** The text in a leading brace pair (braces may nest) and what follows it; null and [text] when it has none. */
    fun braced(text: String): Pair<String?, String> {
        val trimmed = text.trimStart()
        if (!trimmed.startsWith("{")) return null to trimmed
        val end = matching(trimmed, 0, '{', '}') ?: return null to trimmed
        return trimmed.substring(1, end).trim() to trimmed.substring(end + 1).trimStart()
    }

    /** The index of the [close] matching the [open] at [start], or null. */
    fun matching(text: String, start: Int, open: Char, close: Char): Int? {
        var depth = 0
        for (index in start until text.length) {
            when (text[index]) {
                open -> depth++
                close -> if (--depth == 0) return index
            }
        }
        return null
    }
}
