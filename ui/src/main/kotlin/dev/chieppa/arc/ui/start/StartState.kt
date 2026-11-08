package dev.chieppa.arc.ui.start

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dev.chieppa.arc.core.recent.RecentSources
import dev.chieppa.arc.core.start.StartColumns
import dev.chieppa.arc.core.start.StartLayout
import dev.chieppa.arc.core.start.StartScreenKind
import dev.chieppa.arc.core.start.StartScreenLayout

/**
 * The start screen's state: its contributors, the person's [layout] of each screen, the [recent] sources, and the
 * screen last drawn ([kind] and what it [showing]), which the card menus act on. [now] is the time in epoch seconds.
 */
class StartState(
    layout: StartLayout = StartLayout(),
    recent: RecentSources = RecentSources(),
    private val now: () -> Long = { System.currentTimeMillis() / MILLIS },
) {
    val registry = StartRegistry()
    var layout by mutableStateOf(layout)
    var recent by mutableStateOf(recent)
    var kind by mutableStateOf(StartScreenKind.NothingOpen)
    var showing by mutableStateOf(StartColumns())

    /** The person's changes to the screen showing. */
    val screen: StartScreenLayout get() = layout.of(kind)

    /** Changes the screen showing's layout with [edit]. */
    fun arrange(edit: (StartScreenLayout) -> StartScreenLayout) {
        layout = layout.with(kind, edit(screen))
    }

    /** Puts the source [ref], titled [title] where known, first among the recent ones. */
    fun record(ref: String, title: String?) {
        recent = recent.record(ref, title, now())
    }

    fun forget(ref: String) {
        recent = recent.remove(ref)
    }

    fun clearRecent() {
        recent = RecentSources()
    }

    private companion object {
        const val MILLIS = 1000L
    }
}
