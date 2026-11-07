package dev.chieppa.arc.host.github.search

import java.net.URI
import java.net.URISyntaxException
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

/**
 * Reads a pasted GitHub search link as a saved filter's query and a name to start it with:
 * - `github.com/pulls?q=…` and `github.com/issues?q=…`, the person's own lists;
 * - `github.com/search?q=…&type=pullrequests` (or `type=issues`, or no type);
 * - `github.com/owner/name/pulls?q=…` and `/issues?q=…`, with `repo:owner/name` added unless the query names a repo.
 *
 * The name is the query without its `is:`, `type:`, `sort:` and `archived:` qualifiers, cut to [NAME_MAX] characters,
 * or the repo for a repo's list; null when nothing is left, for the caller to name it.
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
    private val LISTS = setOf("pulls", "issues")
    private val SEARCH_TYPES = setOf("pullrequests", "issues")
    private val UNNAMED = Regex("^-?(is|type|sort|archived):")

    /** What [link] searches for, or null when it is not a GitHub search link. */
    fun parse(link: String): PastedSearch? {
        val uri = uri(link.trim()) ?: return null
        if (uri.host?.lowercase()?.removePrefix(WWW) != HOST) return null
        val params = params(uri.rawQuery.orEmpty())
        val query = params[QUERY]?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        val segments = uri.rawPath.orEmpty().split('/').filter { it.isNotEmpty() }
        return when {
            segments == listOf(SEARCH) -> params[TYPE]?.lowercase().let { type ->
                if (type == null || type in SEARCH_TYPES) PastedSearch(query, nameOf(query)) else null
            }
            segments.size == 1 && segments[0] in LISTS -> PastedSearch(query, nameOf(query))
            segments.size == 3 && segments[2] in LISTS -> {
                val repo = "${segments[0]}/${segments[1]}"
                val named = query.split(' ').any { it.startsWith(REPO) }
                PastedSearch(if (named) query else "$query $REPO$repo", repo)
            }
            else -> null
        }
    }

    /** A name for a filter on [query]: its words that are not type or sort qualifiers, cut short; null when none are. */
    fun nameOf(query: String): String? {
        val words = query.split(' ').filter { it.isNotBlank() && !UNNAMED.containsMatchIn(it) }
        val text = words.joinToString(" ").takeIf { it.isNotEmpty() } ?: return null
        return if (text.length <= NAME_MAX) text else text.take(NAME_MAX - 1).trimEnd() + ELLIPSIS
    }

    private fun uri(text: String): URI? = try {
        URI(if ("://" in text) text else "https://$text")
    } catch (_: URISyntaxException) {
        null
    }

    private fun params(raw: String): Map<String, String> = raw.split('&').filter { '=' in it }.associate { pair ->
        decode(pair.substringBefore('=')) to decode(pair.substringAfter('='))
    }

    private fun decode(text: String): String = try {
        URLDecoder.decode(text, StandardCharsets.UTF_8)
    } catch (_: IllegalArgumentException) {
        text
    }
}
