package dev.chieppa.arc.ui.pulls

import dev.chieppa.arc.core.watch.NotifyRule
import dev.chieppa.arc.core.watch.Watchlist

/**
 * The searches one poll asks GitHub for, by key: every saved filter's query under its id, and for each labelled rule
 * that is on, its open pull requests with the label in its repos, under the rule's id.
 */
object WatchSearches {
    private const val OPEN = "is:open"
    private const val LABEL = "label:"
    private const val REPO = "repo:"

    fun of(watchlist: Watchlist): Map<String, String> {
        val filters = watchlist.filters.filter { it.query.isNotBlank() }.associate { it.id to it.query }
        val labelled = watchlist.rules.filterIsInstance<NotifyRule.NewLabelled>().filter { it.on && it.label.isNotBlank() }
            .associate { it.id to labelQuery(it) }
        return filters + labelled
    }

    /** The query for [rule]'s pull requests: open, with its label, in any of its repos. */
    fun labelQuery(rule: NotifyRule.NewLabelled): String {
        val label = if (rule.label.any(Char::isWhitespace)) "\"${rule.label}\"" else rule.label
        return (listOf(OPEN, LABEL + label) + rule.repos.map { REPO + it }).joinToString(" ")
    }
}
