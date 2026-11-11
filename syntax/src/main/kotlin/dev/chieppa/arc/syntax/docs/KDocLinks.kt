package dev.chieppa.arc.syntax.docs

/** Rewrites KDoc's bracket references into symbol links, leaving code and real Markdown links alone. */
internal object KDocLinks {
    private val reference = Regex("""\[([^\]\n]+)]\[([^\]\n]+)]|\[([A-Za-z_][\w.$]*)](?![(\[:])""")

    fun rewrite(text: String): String = MarkdownCode.mapProse(text) { prose ->
        reference.replace(prose) { match ->
            val target = match.groupValues[2].ifEmpty { match.groupValues[3] }
            val label = match.groupValues[1].ifEmpty { match.groupValues[3] }
            SymbolLinks.render(target, label)
        }
    }
}
