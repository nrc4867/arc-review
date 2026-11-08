package dev.chieppa.arc.ui.conversation

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dev.chieppa.arc.core.conversation.ReviewThread
import dev.chieppa.arc.core.conversation.ThreadState

/**
 * How the person has the threads on screen: which are folded open or shut against their state's default, which guest
 * notes they dismissed or adopted as their own drafts, the line whose threads they are choosing among, and the thread
 * a pane is to scroll to.
 */
@Stable
class ThreadViews {
    var toggled by mutableStateOf(emptySet<String>())
    var dismissed by mutableStateOf(emptySet<String>())
    var choosing by mutableStateOf<LineChoice?>(null)
    var reveal by mutableStateOf<ThreadReveal?>(null)

    /**
     * Whether [thread] shows open. A fold holds for the state it was made in, so a thread that changes state, such as
     * one resolved or a comment staged, shows as that state does by default.
     */
    fun expanded(thread: ReviewThread): Boolean = (thread.state in OPEN_BY_DEFAULT) != (key(thread.id, thread.state) in toggled)

    fun toggle(thread: ReviewThread) {
        val key = key(thread.id, thread.state)
        toggled = if (key in toggled) toggled - key else toggled + key
    }

    /** Unfolds thread [id], which is in [state]. */
    fun open(id: String, state: ThreadState) {
        toggled = if (state in OPEN_BY_DEFAULT) toggled - key(id, state) else toggled + key(id, state)
    }

    /** Drops the folds made on thread [id], so it shows as its state does by default. */
    fun forget(id: String) {
        toggled = toggled.filterNot { it.startsWith("$id/") }.toSet()
    }

    private fun key(id: String, state: ThreadState) = "$id/${state.name}"

    /** [threads] as shown: dismissed and adopted guest notes left out. */
    fun shown(threads: List<ReviewThread>): List<ReviewThread> = threads.filter { it.id !in dismissed }

    private companion object {
        val OPEN_BY_DEFAULT = setOf(ThreadState.Open, ThreadState.Draft, ThreadState.Note, ThreadState.Guest)
    }
}
