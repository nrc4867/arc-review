package dev.chieppa.arc.ui.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * The places a person left, to go back to, and those they went back from, to go forward to again, as a browser keeps
 * them. Leaving a place drops the places ahead; places near each other count as one; each list keeps its last [limit].
 */
class NavigationHistory(private val limit: Int = NavigationSizes.HISTORY_LIMIT) {
    /** The places behind, the most recent last. */
    var behind by mutableStateOf(emptyList<NavPlace>())
        private set

    /** The places ahead, the nearest last. */
    var ahead by mutableStateOf(emptyList<NavPlace>())
        private set

    /** Records [place] as left for somewhere else. */
    fun leave(place: NavPlace) {
        behind = (behind.filterIndexed { index, it -> index != behind.lastIndex || !it.near(place) } + place).takeLast(limit)
        ahead = emptyList()
    }

    /** The place to go back to from [here], which goes ahead; null when there is none. */
    fun back(here: NavPlace?): NavPlace? {
        val (to, rest) = step(behind, here) ?: return null
        behind = rest
        here?.let { ahead = (ahead + it).takeLast(limit) }
        return to
    }

    /** The place to go forward to from [here], which goes behind; null when there is none. */
    fun forward(here: NavPlace?): NavPlace? {
        val (to, rest) = step(ahead, here) ?: return null
        ahead = rest
        here?.let { behind = (behind + it).takeLast(limit) }
        return to
    }

    /** The last of [places] that is not where the person is already, and those before it. */
    private fun step(places: List<NavPlace>, here: NavPlace?): Pair<NavPlace, List<NavPlace>>? {
        val left = places.dropLastWhile { here != null && it.near(here) }
        val to = left.lastOrNull() ?: return null
        return to to left.dropLast(1)
    }
}
