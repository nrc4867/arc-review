package dev.chieppa.arc.store.watch

import dev.chieppa.arc.core.watch.FilterGrouping
import dev.chieppa.arc.core.watch.NotifyRule
import dev.chieppa.arc.core.watch.SavedFilter
import dev.chieppa.arc.core.watch.Watchlist
import dev.chieppa.arc.store.files.StoreJson
import java.nio.file.Files
import java.nio.file.Path

/**
 * The saved filters and notification rules, which every review shares, kept in a JSON file at [path]. A filter or rule
 * that does not read is left out.
 */
class WatchlistFile(private val path: Path) {
    /** Whether the file is there; a first run has none. */
    fun exists(): Boolean = Files.exists(path)

    fun load(): Watchlist {
        val stored = StoreJson.load(path, StoredWatchlist.serializer()) ?: return Watchlist()
        val filters = stored.filters.filter { it.id.isNotBlank() }.distinctBy { it.id }.map { filter ->
            val grouping = FilterGrouping.entries.firstOrNull { it.name == filter.grouping } ?: FilterGrouping.Label
            SavedFilter(filter.id, filter.name, filter.query, grouping)
        }
        val rules = stored.rules.filter { it.id.isNotBlank() }.distinctBy { it.id }.map(::toRule)
        return Watchlist(filters, rules, stored.seen.filterKeys { id -> filters.any { it.id == id } })
    }

    fun save(watchlist: Watchlist) {
        val stored = StoredWatchlist(
            filters = watchlist.filters.map { StoredFilter(it.id, it.name, it.query, it.grouping.name) },
            rules = watchlist.rules.map(::toStored),
            seen = watchlist.seen,
        )
        StoreJson.save(path, StoredWatchlist.serializer(), stored)
    }

    private fun toRule(rule: StoredRule): NotifyRule = when (rule) {
        is StoredRule.Comments -> NotifyRule.CommentsOnMyPullRequests(rule.id, rule.on)
        is StoredRule.Mentions -> NotifyRule.Mentions(rule.id, rule.on)
        is StoredRule.InFilter -> NotifyRule.NewInFilter(rule.id, rule.filter, rule.on)
        is StoredRule.Labelled -> NotifyRule.NewLabelled(rule.id, rule.label, rule.repos, rule.on)
    }

    private fun toStored(rule: NotifyRule): StoredRule = when (rule) {
        is NotifyRule.CommentsOnMyPullRequests -> StoredRule.Comments(rule.id, rule.on)
        is NotifyRule.Mentions -> StoredRule.Mentions(rule.id, rule.on)
        is NotifyRule.NewInFilter -> StoredRule.InFilter(rule.id, rule.filter, rule.on)
        is NotifyRule.NewLabelled -> StoredRule.Labelled(rule.id, rule.label, rule.repos, rule.on)
    }
}
