package dev.chieppa.arc.syntax.docs

/** Rewrites JSDoc and TSDoc inline link tags into Markdown links, leaving code alone. */
internal object JsDocLinks {
    private val link = Regex("""(?:\[([^\]\n]+)])?\{@(link|linkcode|linkplain)\s+([^}]+)}""")

    fun rewrite(text: String): String = MarkdownCode.mapProse(text) { prose ->
        link.replace(prose) { match ->
            val (target, written) = SymbolLinks.splitBody(match.groupValues[3])
            val label = match.groupValues[1].ifEmpty { written }
                ?: if (match.groupValues[2] == "linkplain" || SymbolLinks.isUrl(target)) target else MarkdownCode.span(target)
            SymbolLinks.render(target, label)
        }
    }
}
