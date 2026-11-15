package dev.chieppa.arc.host.github.search

/**
 * Checks a GitHub search query without asking GitHub: quotes closed, parentheses paired and every qualifier given a
 * value. GitHub itself judges the rest when the filter is polled.
 */
object SearchQueries {
    private const val QUOTE = '"'
    private const val OPEN = '('
    private const val CLOSE = ')'
    private val EMPTY_QUALIFIER = Regex("^-?[A-Za-z][A-Za-z-]*:$")

    /** What is wrong with [query], or null when nothing obvious is. */
    fun problem(query: String): SearchReading.Refused? {
        if (query.isBlank()) return SearchReading.Refused(SearchProblem.Empty)
        if (query.count { it == QUOTE } % 2 != 0) return SearchReading.Refused(SearchProblem.UnclosedQuote)
        val words = words(query)
        words.firstOrNull { EMPTY_QUALIFIER.matches(it) }?.let { return SearchReading.Refused(SearchProblem.EmptyQualifier, it) }
        if (!paired(words)) return SearchReading.Refused(SearchProblem.UnbalancedParentheses)
        return null
    }

    /** [query] split on spaces outside quotes, each quoted run kept whole with its quotes. */
    fun words(query: String): List<String> {
        val words = mutableListOf<String>()
        val word = StringBuilder()
        var quoted = false
        for (char in query) {
            when {
                char == QUOTE -> {
                    quoted = !quoted
                    word.append(char)
                }
                char.isWhitespace() && !quoted -> {
                    if (word.isNotEmpty()) words += word.toString()
                    word.clear()
                }
                else -> word.append(char)
            }
        }
        if (word.isNotEmpty()) words += word.toString()
        return words
    }

    private fun paired(words: List<String>): Boolean {
        var depth = 0
        for (char in words.filterNot { it.startsWith(QUOTE) }.joinToString(" ")) {
            if (char == OPEN) depth++
            if (char == CLOSE) depth--
            if (depth < 0) return false
        }
        return depth == 0
    }
}
