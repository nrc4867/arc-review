package dev.chieppa.arc.syntax.docs

/** Turns the HTML and inline tags of a JavaDoc text into Markdown. */
internal object JavaDocHtml {
    private val htmlTag = Regex("<(/?)([a-zA-Z][a-zA-Z0-9]*)((?:\"[^\"]*\"|'[^']*'|[^>\"'])*)>")
    private val anyTag = Regex("<[^>]*>")
    private val href = Regex("href\\s*=\\s*(?:\"([^\"]*)\"|'([^']*)'|([^\\s>]+))", RegexOption.IGNORE_CASE)
    private val blankRuns = Regex("\n{3,}")

    fun toMarkdown(source: String): String {
        val out = StringBuilder()
        val lists = ArrayDeque<Boolean>()
        val anchors = ArrayDeque<String?>()
        val codes = ArrayDeque<Boolean>()
        var i = 0
        while (i < source.length) {
            val char = source[i]
            val inline = if (char == '{') InlineTag.parse(source, i) else null
            val tag = if (char == '<') htmlTag.matchAt(source, i) else null
            val entity = if (char == '&') HtmlEntities.pattern.matchAt(source, i) else null
            when {
                inline != null -> {
                    out.append(JavaDocInline.render(inline))
                    i = inline.end
                }
                tag != null -> i = handleTag(source, tag, out, lists, anchors, codes)
                entity != null -> {
                    out.append(HtmlEntities.decodeOne(entity))
                    i = entity.range.last + 1
                }
                else -> {
                    out.append(char)
                    i++
                }
            }
        }
        return out.toString().replace(blankRuns, "\n\n").trim()
    }

    private fun handleTag(
        source: String,
        tag: MatchResult,
        out: StringBuilder,
        lists: ArrayDeque<Boolean>,
        anchors: ArrayDeque<String?>,
        codes: ArrayDeque<Boolean>,
    ): Int {
        val closing = tag.groupValues[1].isNotEmpty()
        val name = tag.groupValues[2].lowercase()
        val next = tag.range.last + 1
        when (name) {
            "p" -> out.append("\n\n")
            "br" -> out.append("  \n")
            "code", "tt" -> if (closing) {
                if (codes.removeLastOrNull() != true) out.append('`')
            } else {
                val wrapsInline = source.startsWith("{@code", next)
                codes.addLast(wrapsInline)
                if (!wrapsInline) out.append('`')
            }
            "b", "strong" -> out.append("**")
            "i", "em" -> out.append('*')
            "ul", "ol" -> {
                if (closing) lists.removeLastOrNull() else lists.addLast(name == "ol")
                out.append('\n')
            }
            "li" -> if (!closing) out.append('\n').append(if (lists.lastOrNull() == true) "1. " else "- ")
            "a" -> if (closing) {
                anchors.removeLastOrNull()?.let { out.append("](").append(it).append(')') }
            } else {
                val target = hrefOf(tag.groupValues[3])
                anchors.addLast(target)
                if (target != null) out.append('[')
            }
            "pre" -> if (!closing) return preBlock(source, next, out)
        }
        return next
    }

    private fun hrefOf(attributes: String): String? =
        href.find(attributes)?.groupValues?.drop(1)?.firstOrNull { it.isNotEmpty() }

    private fun preBlock(source: String, start: Int, out: StringBuilder): Int {
        val close = source.indexOf(DocSyntax.PRE_CLOSE, start, ignoreCase = true)
        val end = if (close < 0) source.length else close
        out.append("\n\n").append(DocSyntax.FENCE).append("java\n").append(code(source.substring(start, end)))
        out.append('\n').append(DocSyntax.FENCE).append("\n\n")
        return if (close < 0) source.length else close + DocSyntax.PRE_CLOSE.length
    }

    private fun code(inner: String): String {
        val trimmed = inner.trim()
        val literal = if (trimmed.startsWith("{@code")) {
            InlineTag.parse(trimmed, 0)?.takeIf { it.end == trimmed.length }?.rawBody
        } else {
            null
        }
        val text = literal ?: HtmlEntities.decode(inner.replace(anyTag, ""))
        return text.trimIndent().trim('\n')
    }
}
