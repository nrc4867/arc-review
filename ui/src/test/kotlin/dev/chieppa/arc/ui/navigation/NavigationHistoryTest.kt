package dev.chieppa.arc.ui.navigation

import dev.chieppa.arc.core.source.RepoName
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class NavigationHistoryTest {
    private fun place(path: String, line: Int) = NavPlace(RepoName("api"), "a1", path, line)

    @Test
    fun backAndForwardWalkThePlacesLeft() {
        val history = NavigationHistory()
        history.leave(place("A.kt", 10))
        history.leave(place("B.kt", 20))
        val here = place("C.kt", 30)
        assertEquals(place("B.kt", 20), history.back(here))
        assertEquals(place("A.kt", 10), history.back(place("B.kt", 20)))
        assertNull(history.back(place("A.kt", 10)))
        assertEquals(place("B.kt", 20), history.forward(place("A.kt", 10)))
        assertEquals(here, history.forward(place("B.kt", 20)))
        assertNull(history.forward(here))
    }

    @Test
    fun leavingAPlaceDropsThePlacesAhead() {
        val history = NavigationHistory()
        history.leave(place("A.kt", 10))
        history.back(place("B.kt", 5))
        history.leave(place("A.kt", 10))
        assertEquals(emptyList(), history.ahead)
    }

    @Test
    fun placesNearEachOtherCountAsOne() {
        val history = NavigationHistory()
        history.leave(place("A.kt", 10))
        history.leave(place("A.kt", 12))
        assertEquals(listOf(place("A.kt", 12)), history.behind)
        // Going back from right beside a place left skips it.
        history.leave(place("B.kt", 1))
        assertEquals(place("A.kt", 12), history.back(place("B.kt", 2)))
    }

    @Test
    fun itKeepsItsLastPlacesOnly() {
        val history = NavigationHistory(limit = 2)
        (1..4).forEach { history.leave(place("F$it.kt", 1)) }
        assertEquals(listOf("F3.kt", "F4.kt"), history.behind.map { it.path })
    }
}
