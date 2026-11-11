package dev.chieppa.arc.syntax.docs

/** Markdown code spans and fences, which rewriting must leave alone. */
internal object MarkdownCode {
    private val code = Regex("(?m)^[ \\t]*```[\\s\\S]*?(?:^[ \\t]*```|\\z)|(`+)[^`\\n]*?\\1")

    /** [text] as a code span, with a longer fence when it holds a backtick. */
    fun span(text: String): String = if (text.contains(DocSyntax.BACKTICK)) "`` $text ``" else "`$text`"

    /** [text] with [transform] applied to everything outside code spans and fenced blocks. */
    fun mapProse(text: String, transform: (String) -> String): String {
        val out = StringBuilder()
        var last = 0
        for (match in code.findAll(text)) {
            out.append(transform(text.substring(last, match.range.first))).append(match.value)
            last = match.range.last + 1
        }
        return out.append(transform(text.substring(last))).toString()
    }
}
