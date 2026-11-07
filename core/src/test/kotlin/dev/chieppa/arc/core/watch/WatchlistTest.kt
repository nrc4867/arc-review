package dev.chieppa.arc.core.watch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class WatchlistTest {
    private val seeded = WatchSeeds.create("Review requested", "My PRs")

    @Test
    fun aFirstRunHasTheTwoFiltersAndTheTwoAnyRepoRules() {
        assertEquals(listOf(WatchSeeds.REVIEW_REQUESTED, WatchSeeds.MINE), seeded.filters.map { it.query })
        assertEquals(listOf("Review requested", "My PRs"), seeded.filters.map { it.name })
        assertEquals(2, seeded.rules.size)
        assertEquals(true, seeded.rules.all { it.on })
    }

    @Test
    fun newIdsAreFreeAcrossFiltersAndRules() {
        assertEquals("f3", seeded.nextId(Watchlist.FILTER_PREFIX))
        assertEquals("r3", seeded.nextId(Watchlist.RULE_PREFIX))
    }

    @Test
    fun removingAFilterTakesItsRulesAndWhenItWasSeen() {
        val watch = seeded
            .withRule(NotifyRule.NewInFilter("r3", "f1"))
            .seen("f1", 10L)
            .withoutFilter("f1")
        assertNull(watch.filter("f1"))
        assertEquals(2, watch.rules.size)
        assertEquals(emptyMap(), watch.seen)
    }

    @Test
    fun aFilterOrRuleWithTheSameIdIsReplacedInPlace() {
        val renamed = seeded.withFilter(seeded.filters[0].copy(name = "Waiting on me"))
        assertEquals("Waiting on me", renamed.filters[0].name)
        val off = renamed.withRule(renamed.rules[1].switched(false))
        assertEquals(listOf(true, false), off.rules.map { it.on })
    }
}
