package dev.chieppa.arc.syntax.highlight

import dev.chieppa.arc.syntax.facts.ParseErrors
import dev.chieppa.arc.syntax.grammar.Grammar
import dev.chieppa.arc.syntax.grammar.LanguageFamily
import dev.chieppa.arc.syntax.runtime.QueryKind
import dev.chieppa.arc.syntax.runtime.SyntaxQueries
import dev.chieppa.arc.syntax.runtime.SyntaxTree

/**
 * Highlight spans from a grammar's highlights query. Where captures overlap the narrower wins, and on the same node
 * the later pattern wins, which is what the query files are written for. Name-like styles paint only nodes without
 * named children, so `(primary_constructor) @constructor` does not colour a whole construct.
 */
object Highlighter {
    /** Above this share of characters under parse errors a SQL file is left to the viewer's own tokenizer. */
    private const val SQL_ERROR_SHARE = 0.2

    /** Spans for [text] in [grammar], or null when tree-sitter's reading of it is too broken to colour by. */
    fun highlight(grammar: Grammar, text: String): List<HighlightSpan>? = SyntaxTree.parse(grammar, text).use { highlight(it) }

    fun highlight(tree: SyntaxTree): List<HighlightSpan>? {
        val text = tree.text
        if (tree.grammar.family == LanguageFamily.Sql && text.isNotEmpty() && ParseErrors.chars(tree) > text.length * SQL_ERROR_SHARE) return null
        val query = SyntaxQueries.of(tree.grammar, QueryKind.Highlights) ?: return emptyList()
        val styles = query.captureNames.map(CaptureStyles::of)
        class Paint(val start: Int, val end: Int, val pattern: Int, val style: TokenStyle?)
        val paints = ArrayList<Paint>()
        val records = query.run(tree)
        for (i in 0 until records.size) {
            val resolved = styles[records.capture(i)]
            if (resolved is CaptureStyles.Resolved.Ignored) continue
            val style = (resolved as? CaptureStyles.Resolved.Styled)?.style
            if (style != null && style != TokenStyle.String && style != TokenStyle.Comment && records.namedChildren(i) > 0) continue
            var start = records.start(i)
            var end = minOf(records.end(i), text.length)
            while (start < end && text[start].isWhitespace()) start++
            while (end > start && text[end - 1].isWhitespace()) end--
            if (end > start) paints += Paint(start, end, records.pattern(i), style)
        }
        paints.sortWith(compareByDescending<Paint> { it.end - it.start }.thenBy { it.pattern })
        val canvas = arrayOfNulls<TokenStyle>(text.length)
        for (paint in paints) for (i in paint.start until paint.end) canvas[i] = paint.style
        return buildList {
            var i = 0
            while (i < canvas.size) {
                val style = canvas[i]
                if (style == null) {
                    i++
                    continue
                }
                var j = i + 1
                while (j < canvas.size && canvas[j] == style) j++
                add(HighlightSpan(i, j, style))
                i = j
            }
        }
    }
}
