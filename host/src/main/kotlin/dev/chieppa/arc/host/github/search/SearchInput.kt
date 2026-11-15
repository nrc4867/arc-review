package dev.chieppa.arc.host.github.search

/**
 * Reads what a person typed or pasted to make a saved filter: a link is read by [SearchUrls], anything else is taken
 * as a GitHub search query and checked by [SearchQueries]. A query is named by [SearchUrls.nameOf].
 */
object SearchInput {
    private val LINK = Regex("""^([a-z][a-z0-9+.-]*://|(www\.)?github\.com\b|[\w-]+(\.[\w-]+)*\.[a-z]{2,}/)""", RegexOption.IGNORE_CASE)
    private val SPACES = Regex("""\s+""")

    fun read(text: String): SearchReading {
        val trimmed = text.trim()
        if (isLink(trimmed)) return SearchUrls.read(trimmed)
        val query = trimmed.split(SPACES).joinToString(" ")
        return SearchQueries.problem(query) ?: SearchReading.Found(PastedSearch(query, SearchUrls.nameOf(query)))
    }

    /** Whether [text] is a link rather than a query: it starts with a scheme, with github.com, or with a host and a path. */
    fun isLink(text: String): Boolean = LINK.containsMatchIn(text.trim())
}
