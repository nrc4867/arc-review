package dev.chieppa.arc.ui.conversation

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dev.chieppa.arc.core.conversation.ReviewThread
import dev.chieppa.arc.core.conversation.ThreadState

/**
 * How the person has the threads on screen: which are folded open or shut against their state's default, and which
 * guest notes they dismissed or adopted as their own drafts.
 */
@Stable
class ThreadViews {
    var toggled by mutableStateOf(emptySet<String>())
    var dismissed by mutableStateOf(emptySet<String>())

    fun expanded(thread: ReviewThread): Boolean = (thread.state in OPEN_BY_DEFAULT) != (thread.id in toggled)

    fun toggle(thread: ReviewThread) {
        toggled = if (thread.id in toggled) toggled - thread.id else toggled + thread.id
    }

    /** [threads] as shown: dismissed and adopted guest notes left out. */
    fun shown(threads: List<ReviewThread>): List<ReviewThread> = threads.filter { it.id !in dismissed }

    private companion object {
        val OPEN_BY_DEFAULT = setOf(ThreadState.Open, ThreadState.Draft, ThreadState.Note, ThreadState.Guest)
    }
}
