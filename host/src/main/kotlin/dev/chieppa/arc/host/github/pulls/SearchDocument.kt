package dev.chieppa.arc.host.github.pulls

/**
 * One GraphQL document holding every search to poll, each as an aliased `search` field, with the rate budget: GitHub
 * charges the whole document about one point. Searches are by key, in order; [aliases] maps each alias back to its
 * key. A query that names no type gets `is:pr`, so it finds pull requests only.
 */
class SearchDocument(searches: Map<String, String>, first: Int = FIRST) {
    val aliases: Map<String, String> = searches.keys.mapIndexed { index, key -> "$ALIAS_PREFIX$index" to key }.toMap()

    val text: String = buildString {
        append("query {\n  rateLimit { limit remaining resetAt }\n")
        aliases.forEach { (alias, key) ->
            val query = withType(searches.getValue(key))
            append("  $alias: search(type: ISSUE, first: $first, query: ${quote(query)}) { issueCount nodes { ...${PullRequestFragment.NAME} } }\n")
        }
        append("}\n")
        append(PullRequestFragment.TEXT)
    }

    companion object {
        /** How many pull requests each search brings back. */
        const val FIRST = 50
        private const val ALIAS_PREFIX = "s"
        private val TYPED = Regex("(^|\\s)(is|type):(pr|pull-request|issue)\\b")
        private const val PR = "is:pr "

        /** [query] with `is:pr` in front unless it names a type itself. */
        fun withType(query: String): String = if (TYPED.containsMatchIn(query)) query.trim() else PR + query.trim()

        /** [text] as a GraphQL string literal. */
        fun quote(text: String): String = buildString {
            append('"')
            for (c in text) {
                when (c) {
                    '"' -> append("\\\"")
                    '\\' -> append("\\\\")
                    '\n' -> append("\\n")
                    '\r' -> append("\\r")
                    '\t' -> append("\\t")
                    else -> if (c < ' ') append("\\u%04x".format(c.code)) else append(c)
                }
            }
            append('"')
        }
    }
}
