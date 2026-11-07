package dev.chieppa.arc.ui

import dev.chieppa.arc.core.watch.FilterGrouping
import dev.chieppa.arc.core.watch.NotifyRule
import dev.chieppa.arc.core.watch.SavedFilter
import dev.chieppa.arc.core.watch.Watchlist
import dev.chieppa.arc.host.github.notifications.NotificationThread
import dev.chieppa.arc.ui.FakePulls.auth
import dev.chieppa.arc.ui.FakePulls.pull
import dev.chieppa.arc.ui.FakePulls.security
import dev.chieppa.arc.ui.pulls.FilterResult
import dev.chieppa.arc.ui.pulls.PullGroups
import dev.chieppa.arc.ui.pulls.PullWatch
import dev.chieppa.arc.ui.pulls.WatchEvent
import dev.chieppa.arc.ui.pulls.WatchEvents
import dev.chieppa.arc.ui.pulls.WatchSearches
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PullWatchTest {
    private val team = SavedFilter("f1", "Team: auth", "org:lumen label:auth")
    private val labelled = NotifyRule.NewLabelled("r2", "security", listOf("lumen/api", "lumen/web"))
    private val watchlist = Watchlist(listOf(team), listOf(NotifyRule.NewInFilter("r1", "f1"), labelled))

    @Test
    fun aPollSearchesEveryFilterAndEachLabelledRuleThatIsOn() {
        assertEquals(
            mapOf("f1" to "org:lumen label:auth", "r2" to "is:open label:security repo:lumen/api repo:lumen/web"),
            WatchSearches.of(watchlist),
        )
        assertEquals(setOf("f1"), WatchSearches.of(watchlist.withRule(labelled.switched(false))).keys)
    }

    @Test
    fun aPullRequestNewToASearchSetsOffItsRuleOnceButAFirstPollSetsOffNothing() {
        val old = pull(1, "Old")
        val new = pull(2, "New", listOf(auth, security))
        val after = mapOf("f1" to listOf(old, new), "r2" to listOf(new))
        assertEquals(emptyList(), WatchEvents.newPulls(watchlist.rules, emptyMap(), after))
        val before = mapOf("f1" to FilterResult(1, listOf(old), 0), "r2" to FilterResult(0, emptyList(), 0))
        val events = WatchEvents.newPulls(watchlist.rules, before, after)
        assertEquals(listOf(WatchEvent.NewPull(watchlist.rules[0], new)), events)
        val off = watchlist.rules.map { it.switched(false) }
        assertEquals(emptyList(), WatchEvents.newPulls(off, before, after))
    }

    @Test
    fun commentsOnMyPullRequestsAndMentionsComeFromNotifications() {
        val rules = listOf(NotifyRule.CommentsOnMyPullRequests("r1"), NotifyRule.Mentions("r2", on = false))
        val at = Instant.parse("2026-11-06T10:00:00Z")
        val comment = NotificationThread("1", NotificationThread.AUTHOR, true, at, "Rotate", NotificationThread.PULL_REQUEST, FakePulls.api, 412)
        val mention = comment.copy(id = "2", reason = NotificationThread.MENTION)
        val other = comment.copy(id = "3", reason = "subscribed")
        assertEquals(listOf(WatchEvent.Thread(rules[0], comment)), WatchEvents.threads(rules, listOf(comment, mention, other)))
    }

    @Test
    fun byLabelEachPullRequestSitsUnderItsFirstLabelBiggestGroupFirst() {
        val pulls = listOf(pull(1, "A", listOf(security)), pull(2, "B"), pull(3, "C", listOf(auth, security)), pull(4, "D", emptyList()))
        val groups = PullGroups.of(pulls, FilterGrouping.Label)
        assertEquals(listOf("auth", "security", null), groups.map { it.name })
        assertEquals(listOf(2, 1, 1), groups.map { it.pulls.size })
        assertEquals(listOf(4), PullGroups.of(pulls, FilterGrouping.None).map { it.pulls.size })
    }

    @Test
    fun whatChangedSinceAFilterWasLookedAtStaysMarkedWhileItsTabShows() {
        var now = 1_000L
        val watch = PullWatch(watchlist.seen("f1", Instant.parse("2026-11-02T00:00:00Z").toEpochMilli()), clock = { now })
        val older = pull(1, "Older", updated = Instant.parse("2026-11-01T00:00:00Z"))
        val newer = pull(2, "Newer", updated = Instant.parse("2026-11-03T00:00:00Z"))
        watch.results = mapOf("f1" to FilterResult(2, listOf(older, newer), 0))
        assertEquals(1, watch.newCount("f1"))
        watch.startLooking("f1")
        assertEquals(1, watch.newCount("f1"))
        assertTrue(watch.isNew("f1", newer))
        now = Instant.parse("2026-11-04T00:00:00Z").toEpochMilli()
        watch.stopLooking("f1")
        assertEquals(0, watch.newCount("f1"))
    }

    @Test
    fun changingAFiltersQueryDropsItsResultAndAsksForAPoll() {
        val watch = PullWatch(watchlist)
        watch.results = mapOf("f1" to FilterResult(0, emptyList(), 0))
        watch.updateFilter(team.copy(query = "label:auth"))
        assertEquals(emptyMap(), watch.results)
        assertTrue(watch.refreshRequests.tryReceive().isSuccess)
    }
}
