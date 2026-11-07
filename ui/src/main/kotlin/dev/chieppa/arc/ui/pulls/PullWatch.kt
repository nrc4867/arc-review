package dev.chieppa.arc.ui.pulls

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dev.chieppa.arc.core.watch.NotifyRule
import dev.chieppa.arc.core.watch.SavedFilter
import dev.chieppa.arc.core.watch.Watchlist
import dev.chieppa.arc.host.github.pulls.PullRequestSummary
import dev.chieppa.arc.host.github.pulls.RateBudget
import dev.chieppa.arc.host.github.pulls.Viewer
import kotlinx.coroutines.channels.Channel

/**
 * The saved filters and notification rules, with what GitHub answered for them: each
 * search's result by key, the rate budget, who the person is, and how polling stands. [unseen] counts notifications
 * since the bell was last opened. Views read it and recompose when it changes; edits that change what is polled ask
 * for a poll at once.
 */
class PullWatch(watchlist: Watchlist = Watchlist(), private val clock: () -> Long = System::currentTimeMillis) {
    var watchlist by mutableStateOf(watchlist)
    var results by mutableStateOf<Map<String, FilterResult>>(emptyMap())
    var budget by mutableStateOf<RateBudget?>(null)
    var viewer by mutableStateOf<Viewer?>(null)
    var status by mutableStateOf(WatchStatus.Waiting)
    var unseen by mutableIntStateOf(0)

    /** When each filter showing in a tab had last been looked at before it showed, by filter id. */
    private var looking by mutableStateOf<Map<String, Long?>>(emptyMap())

    /** Asks for a poll now, rather than at the next interval. */
    val refreshRequests = Channel<Unit>(Channel.CONFLATED)

    fun refresh() {
        refreshRequests.trySend(Unit)
    }

    /** Whether [pull] changed after filter [id] was last looked at; every pull request of a filter never looked at is new. */
    fun isNew(id: String, pull: PullRequestSummary): Boolean {
        val since = if (id in looking) looking[id] else watchlist.seen[id]
        return since == null || pull.updatedAt.toEpochMilli() > since
    }

    /** How many of filter [id]'s pull requests are new since it was looked at. */
    fun newCount(id: String): Int = results[id]?.pulls?.count { isNew(id, it) } ?: 0

    /** Filter [id] starts showing in a tab: what is new stays marked while it shows, and it counts as looked at now. */
    fun startLooking(id: String) {
        if (id in looking) return
        looking = looking + (id to watchlist.seen[id])
        watchlist = watchlist.seen(id, clock())
    }

    /** Filter [id] stops showing; it was looked at until now. */
    fun stopLooking(id: String) {
        looking = looking - id
        if (watchlist.filter(id) != null) watchlist = watchlist.seen(id, clock())
    }

    /** Adds a filter named [name] on [query] and polls; returns it. */
    fun addFilter(name: String, query: String): SavedFilter {
        val filter = SavedFilter(watchlist.nextId(Watchlist.FILTER_PREFIX), name, query.trim())
        watchlist = watchlist.withFilter(filter)
        refresh()
        return filter
    }

    /** Puts [filter] in place of the one with its id, polling when its query changed. */
    fun updateFilter(filter: SavedFilter) {
        val before = watchlist.filter(filter.id)
        watchlist = watchlist.withFilter(filter)
        if (before?.query != filter.query) {
            results = results - filter.id
            refresh()
        }
    }

    fun removeFilter(id: String) {
        watchlist = watchlist.withoutFilter(id)
        results = results - id
    }

    /** Adds [rule], or puts it in place of the rule with its id, polling when it changes what is searched. */
    fun putRule(rule: NotifyRule) {
        watchlist = watchlist.withRule(rule)
        if (rule is NotifyRule.NewLabelled) refresh()
    }

    fun removeRule(id: String) {
        watchlist = watchlist.withoutRule(id)
        results = results - id
    }

    /** An id for a new rule. */
    fun nextRuleId(): String = watchlist.nextId(Watchlist.RULE_PREFIX)

    fun now(): Long = clock()
}
