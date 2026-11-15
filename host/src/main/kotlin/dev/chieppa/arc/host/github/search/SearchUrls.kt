package dev.chieppa.arc.host.github.search

import java.net.URLDecoder
import java.nio.charset.StandardCharsets

/**
 * Reads a pasted GitHub search link as a saved filter's query and a name to start it with:
 * - `github.com/pulls?q=…` and `github.com/issues?q=…`, the person's own lists, and their tabs such as
 *   `/pulls/review-requested`, which without a query search what the tab shows;
 * - `github.com/search?q=…&type=pullrequests` (or `type=issues`, or no type);
 * - `github.com/owner/name/pulls?q=…` and `/issues?q=…`, with `repo:owner/name` added unless the query names a repo,
 *   and the open pull requests or issues without a query.
 *
 * The link is read as typed, so it may hold spaces or characters a browser would encode; `+` and `%20` are spaces,
 * other parameters and a trailing slash are ignored. The name is the query without its `is:`, `type:`, `sort:` and
 * `archived:` qualifiers, cut to [NAME_MAX] characters, or the repo for a repo's list; null when nothing is left, for
 * the caller to name it.
 */
object SearchUrls {
    const val NAME_MAX = 40
    private const val HOST = "github.com"
    private const val WWW = "www."
    private const val QUERY = "q"
    private const val TYPE = "type"
    private const val SEARCH = "search"
    private const val REPO = "repo:"
    private const val ELLIPSIS = "…"
    private const val PULLS = "pulls"
    private val LISTS = setOf(PULLS, "issues")
    private val SEARCH_TYPES = setOf("pullrequests", "issues")
    private val SCHEME = Regex("^[A-Za-z][A-Za-z0-9+.-]*://")
    private val SPACES = Regex("""\s+""")
    private val UNNAMED = Regex("^-?(is|type|sort|archived):")

    /** What the person's own lists search for, by tab, before `is:pr` or `is:issue`; the list itself is their own. */
    private val TABS = mapOf(
        null to "author:@me",
        "created" to "author:@me",
        "assigned" to "assignee:@me",
        "mentioned" to "mentions:@me",
        "review-requested" to "review-requested:@me",
    )

    /** What [link] searches for, or null when it is not a GitHub search link. */
    fun parse(link: String): PastedSearch? = (read(link) as? SearchReading.Found)?.search

    /** What [link] searches for, or why it is no GitHub search link. */
    fun read(link: String): SearchReading {
        val text = link.trim().replace(SCHEME, "").substringBefore('#')
        val address = text.substringBefore('?')
        val host = address.substringBefore('/').substringBefore(':').lowercase().removePrefix(WWW)
        if (host != HOST) return SearchReading.Refused(SearchProblem.NotGitHub)
        val params = params(text.substringAfter('?', ""))
        val query = params[QUERY]?.let(::spaced)?.takeIf { it.isNotEmpty() }
        val segments = address.substringAfter('/', "").split('/').filter { it.isNotEmpty() }.map { it.lowercase() }
        return when {
            segments == listOf(SEARCH) -> {
                val type = params[TYPE]?.lowercase()
                when {
                    type != null && type !in SEARCH_TYPES -> SearchReading.Refused(SearchProblem.NotPullRequests, type)
                    query == null -> SearchReading.Refused(SearchProblem.NoQuery)
                    else -> found(query, nameOf(query))
                }
            }
            segments.size in 1..2 && segments[0] in LISTS -> {
                val tab = TABS[segments.getOrNull(1)]
                when {
                    query != null -> found(query, nameOf(query))
                    tab != null -> listQuery(segments[0], tab).let { found(it, nameOf(it)) }
                    else -> SearchReading.Refused(SearchProblem.NoQuery)
                }
            }
            segments.size == 3 && segments[2] in LISTS -> {
                val repo = address.split('/').filter { it.isNotEmpty() }.let { "${it[1]}/${it[2]}" }
                val asked = query ?: listQuery(segments[2], null)
                val named = SearchQueries.words(asked).any { it.startsWith(REPO) }
                found(if (named) asked else "$asked $REPO$repo", repo)
            }
            else -> SearchReading.Refused(SearchProblem.NotASearch)
        }
    }

    /** A name for a filter on [query]: its words that are not type or sort qualifiers, cut short; null when none are. */
    fun nameOf(query: String): String? {
        val words = query.split(' ').filter { it.isNotBlank() && !UNNAMED.containsMatchIn(it) }
        val text = words.joinToString(" ").takeIf { it.isNotEmpty() } ?: return null
        return if (text.length <= NAME_MAX) text else text.take(NAME_MAX - 1).trimEnd() + ELLIPSIS
    }

    private fun found(query: String, name: String?): SearchReading =
        SearchQueries.problem(query) ?: SearchReading.Found(PastedSearch(query, name))

    /** The open pull requests or issues of [list], narrowed by [tab]'s qualifier. */
    private fun listQuery(list: String, tab: String?): String =
        listOfNotNull("is:open", if (list == PULLS) "is:pr" else "is:issue", tab).joinToString(" ")

    private fun spaced(text: String): String = text.trim().split(SPACES).joinToString(" ")

    private fun params(raw: String): Map<String, String> = raw.split('&').filter { '=' in it }.associate { pair ->
        decode(pair.substringBefore('=')) to decode(pair.substringAfter('='))
    }

    private fun decode(text: String): String = try {
        URLDecoder.decode(text, StandardCharsets.UTF_8)
    } catch (_: IllegalArgumentException) {
        text.replace('+', ' ')
    }
}
