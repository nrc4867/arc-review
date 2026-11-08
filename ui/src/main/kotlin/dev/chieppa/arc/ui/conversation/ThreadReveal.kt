package dev.chieppa.arc.ui.conversation

import dev.chieppa.arc.core.conversation.LineSide
import dev.chieppa.arc.core.source.Source

/** A thread to bring into view: the pane showing [path] of [source] scrolls to [line] on [side]. */
data class ThreadReveal(val source: Source, val path: String, val side: LineSide, val line: Int) {
    companion object {
        /** Lines shown above a revealed thread's line. */
        const val LEAD_IN = 3
    }
}
