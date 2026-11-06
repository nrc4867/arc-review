package dev.chieppa.arc.ui.find

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Finding in one pane: whether the bar is [open], the [query], the [matches] in the pane's lines and which is
 * [current]. [invalid] is true while the query is a regular expression that does not parse. [opened] counts how often
 * the bar was asked for, so it can take the keyboard each time.
 */
@Stable
class FindState {
    var open by mutableStateOf(false)
    var query by mutableStateOf(FindQuery())
    var matches by mutableStateOf(emptyList<FindMatch>())
        private set
    var invalid by mutableStateOf(false)
        private set
    var current by mutableIntStateOf(-1)
        private set
    var opened by mutableIntStateOf(0)
        private set

    fun show() {
        open = true
        opened++
    }

    /** Finds [query] in [lines] again, keeping the current match where it can. */
    fun search(lines: List<String>) {
        val found = FindMatcher.find(lines, query)
        invalid = found == null
        matches = found.orEmpty()
        current = if (matches.isEmpty()) -1 else current.coerceIn(0, matches.lastIndex)
    }

    /** Moves [step] matches on, wrapping round, and returns the match it lands on. */
    fun move(step: Int): FindMatch? {
        if (matches.isEmpty()) return null
        current = (current + step).mod(matches.size)
        return matches[current]
    }

    val currentMatch: FindMatch? get() = matches.getOrNull(current)
}
