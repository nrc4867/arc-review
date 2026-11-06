package dev.chieppa.arc.ui.find

import java.util.regex.PatternSyntaxException

/** Finds a [FindQuery] in lines of text, top to bottom and left to right, matches not overlapping. */
object FindMatcher {
    private const val WORD_BOUNDARY = "\\b"

    /** Every match of [query] in [lines], or null when its regular expression does not parse. */
    fun find(lines: List<String>, query: FindQuery): List<FindMatch>? {
        if (query.text.isEmpty()) return emptyList()
        val pattern = pattern(query) ?: return null
        return lines.flatMapIndexed { index, line ->
            pattern.findAll(line).filter { it.range.last >= it.range.first }.map { FindMatch(index, it.range.first, it.range.last + 1) }.toList()
        }
    }

    private fun pattern(query: FindQuery): Regex? {
        val body = if (query.regex) query.text else Regex.escape(query.text)
        val bounded = if (query.wholeWord) WORD_BOUNDARY + "(?:" + body + ")" + WORD_BOUNDARY else body
        val options = if (query.matchCase) emptySet() else setOf(RegexOption.IGNORE_CASE)
        return try {
            Regex(bounded, options)
        } catch (_: PatternSyntaxException) {
            null
        }
    }
}
