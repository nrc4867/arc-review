package dev.chieppa.arc.syntax.docs

/** Renders JavaDoc inline tags as Markdown. */
internal object JavaDocInline {
    fun render(tag: InlineTag): String = when (tag.name) {
        "code", "value" -> MarkdownCode.span(tag.body)
        "literal" -> tag.body
        "link", "linkplain", "linkcode" -> link(tag)
        "inheritDoc" -> ""
        else -> tag.body
    }

    /** A link to the symbol [target] as written, labelled [label] or its own name in code for a bare reference. */
    fun reference(target: String, label: String?, plain: Boolean = false): String {
        val shown = label ?: displayName(target).let { if (plain) it else MarkdownCode.span(it) }
        return SymbolLinks.render(target, shown)
    }

    private fun link(tag: InlineTag): String {
        val (target, label) = SymbolLinks.splitBody(tag.body)
        return reference(target, label, plain = tag.name == "linkplain")
    }

    private fun displayName(target: String): String =
        target.substringBefore('(').replace('#', '.').removePrefix(".")
}
