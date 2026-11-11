package dev.chieppa.arc.syntax.docs

/** Turns the body of a JSDoc example into Markdown. */
internal object JsDocExample {
    private val caption = Regex("""\s*<caption>(.*)</caption>\s*""")

    /** The example [lines] as an optional caption line followed by a fenced block in [language], or as written when it holds a fence. */
    fun toMarkdown(lines: List<String>, language: String): String {
        val first = lines.indexOfFirst { it.isNotBlank() }
        if (first < 0) return ""
        val captionText = caption.matchEntire(lines[first])?.groupValues?.get(1)?.trim()
        val bodyLines = if (captionText != null) lines.drop(first + 1) else lines
        val body = CommentText.join(bodyLines.dropWhile { it.isBlank() })
        val code = if (body.contains(DocSyntax.FENCE)) body else "${DocSyntax.FENCE}$language\n$body\n${DocSyntax.FENCE}"
        return if (captionText == null) code else "$captionText\n\n$code"
    }
}
