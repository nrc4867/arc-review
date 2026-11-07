package dev.chieppa.arc.store.watch

import dev.chieppa.arc.core.watch.FilterGrouping
import dev.chieppa.arc.core.watch.NotifyRule
import dev.chieppa.arc.core.watch.SavedFilter
import dev.chieppa.arc.core.watch.WatchSeeds
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WatchlistFileTest {
    private val path = Files.createTempDirectory("arc-watch").resolve("filters.json")

    @Test
    fun filtersRulesAndWhenFiltersWereSeenComeBackAsSaved() {
        val file = WatchlistFile(path)
        assertFalse(file.exists())
        val watchlist = WatchSeeds.create("Review requested", "My PRs")
            .withFilter(SavedFilter("f3", "Team: auth", "org:lumen label:auth", FilterGrouping.Repo))
            .withRule(NotifyRule.NewInFilter("r3", "f3"))
            .withRule(NotifyRule.NewLabelled("r4", "security", listOf("lumen/api", "lumen/web"), on = false))
            .seen("f3", 1234L)
        file.save(watchlist)
        assertTrue(file.exists())
        assertEquals(watchlist, WatchlistFile(path).load())
    }

    @Test
    fun aFileThatDoesNotReadIsEmpty() {
        Files.writeString(path, "{ not json")
        assertEquals(emptyList(), WatchlistFile(path).load().filters)
    }
}
