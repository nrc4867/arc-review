package dev.chieppa.arc.syntax.docs

/** Escapes plain comment text so Markdown shows it as written. */
internal object SqlMarkdownEscape {
    private val special = Regex("""[\\*_`\[\]<>]""")
    private val leadingMarker = Regex("""^(?:([#+\-])|(\d+)([.)]))""")

    fun line(text: String): String {
        val escaped = special.replace(text.trim()) { "\\" + it.value }
        return leadingMarker.replace(escaped) { it.groupValues[2] + "\\" + it.groupValues[1] + it.groupValues[3] }
    }
}
