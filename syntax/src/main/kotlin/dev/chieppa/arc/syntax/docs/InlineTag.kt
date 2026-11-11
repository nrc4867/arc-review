package dev.chieppa.arc.syntax.docs

/** An inline tag read from text: its [name], [rawBody] as written and the index [end] after its closing brace. */
internal data class InlineTag(val name: String, val rawBody: String, val end: Int) {
    val body: String get() = rawBody.trim()

    companion object {
        /** The inline tag starting at [start] (an opening brace and `@`), braces inside its body nesting, or null. */
        fun parse(text: String, start: Int): InlineTag? {
            if (!text.startsWith("{@", start)) return null
            var nameEnd = start + 2
            while (nameEnd < text.length && text[nameEnd].isLetterOrDigit()) nameEnd++
            if (nameEnd == start + 2 || nameEnd >= text.length) return null
            if (text[nameEnd] != '}' && !text[nameEnd].isWhitespace()) return null
            val close = TextSplit.matching(text, start, '{', '}') ?: return null
            return InlineTag(text.substring(start + 2, nameEnd), text.substring(nameEnd, close), close + 1)
        }
    }
}
