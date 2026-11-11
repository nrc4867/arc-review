package dev.chieppa.arc.index.hint

import dev.chieppa.arc.codeintel.model.HintLine
import dev.chieppa.arc.index.IndexLimits
import dev.chieppa.arc.syntax.facts.Definition
import dev.chieppa.arc.syntax.facts.SymbolKind
import dev.chieppa.arc.syntax.grammar.Grammar
import dev.chieppa.arc.syntax.grammar.LanguageFamily

/**
 * A definition's signature as written: its text from where the definition starts up to its body, initializer or the
 * end of the declaration, one part per line with blank lines left out and each line trimmed. A JavaScript function
 * held in a variable runs to its arrow, a T-SQL procedure to `AS` and a table to its column list. A signature longer
 * than [IndexLimits.SIGNATURE_CHARS] is cut short with an ellipsis.
 */
internal object SignatureReader {
    fun lines(grammar: Grammar, text: String, definition: Definition): List<HintLine> {
        val start = definition.spanStart.coerceIn(0, text.length)
        val spanEnd = definition.spanEnd.coerceIn(start, text.length)
        val limit = minOf(spanEnd, start + IndexLimits.SIGNATURE_CHARS)
        val end = endOf(grammar, text, start, limit, definition.kind)
        val firstLine = definition.line - text.substring(start, definition.nameStart.coerceIn(start, text.length)).count { it == '\n' }
        val parts = text.substring(start, end).trimEnd().split('\n').mapIndexedNotNull { index, line ->
            line.trim().takeIf { it.isNotEmpty() }?.let { HintLine(it, firstLine + index) }
        }
        val cut = end == limit && limit < spanEnd
        if (!cut || parts.isEmpty()) return parts
        return parts.dropLast(1) + parts.last().let { it.copy(text = it.text + SignatureScan.ELLIPSIS) }
    }

    private fun endOf(grammar: Grammar, text: String, start: Int, limit: Int, kind: SymbolKind): Int {
        val sql = grammar.family == LanguageFamily.Sql
        val arrow = grammar.family == LanguageFamily.JavaScript && kind.isCallable
        var depth = 0
        var i = start
        while (i < limit) {
            val c = text[i]
            val next = text.getOrNull(i + 1)
            when {
                c in SignatureScan.QUOTES && !(sql && c == '`') -> i = skipString(text, i, limit)
                !sql && text.startsWith(SignatureScan.LINE_COMMENT, i) -> i = text.indexOf('\n', i).let { if (it < 0) limit else it }
                text.startsWith(SignatureScan.BLOCK_COMMENT, i) ->
                    i = text.indexOf(SignatureScan.BLOCK_COMMENT_END, i).let { if (it < 0) limit else it + 1 }
                c in SignatureScan.OPEN -> {
                    if (depth == 0 && sql && kind == SymbolKind.Table) return i
                    depth++
                }
                c in SignatureScan.CLOSE -> depth = (depth - 1).coerceAtLeast(0)
                depth > 0 -> Unit
                c == SignatureScan.BODY || c == SignatureScan.END -> return i
                c == SignatureScan.ASSIGN && arrow -> if (next == SignatureScan.ARROW_HEAD) return i
                c == SignatureScan.ASSIGN && isAssignment(text, i) -> return i
                sql && isWord(text, i, SignatureScan.SQL_BODY) -> return i
            }
            i++
        }
        return limit
    }

    /** An `=` that assigns, not part of `==`, `=>`, `!=`, `<=` or `>=`. */
    private fun isAssignment(text: String, at: Int): Boolean {
        val next = text.getOrNull(at + 1)
        val before = text.getOrNull(at - 1)
        return next != SignatureScan.ASSIGN && next != SignatureScan.ARROW_HEAD && (before == null || before !in SignatureScan.NOT_ASSIGN_BEFORE)
    }

    /** The index of the quote closing the string opened at [open], or [limit]. */
    private fun skipString(text: String, open: Int, limit: Int): Int {
        val quote = text[open]
        var i = open + 1
        while (i < limit) {
            if (text[i] == SignatureScan.ESCAPE) i++ else if (text[i] == quote) return i
            i++
        }
        return limit
    }

    private fun isWord(text: String, at: Int, word: String): Boolean =
        text.regionMatches(at, word, 0, word.length, ignoreCase = true) &&
            text.getOrNull(at - 1)?.isLetterOrDigit() != true && text.getOrNull(at + word.length)?.isLetterOrDigit() != true
}
