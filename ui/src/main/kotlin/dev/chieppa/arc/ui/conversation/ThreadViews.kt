package dev.chieppa.arc.ui.conversation

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dev.chieppa.arc.core.conversation.ReviewThread
import dev.chieppa.arc.core.conversation.ThreadState
import dev.chieppa.arc.core.conversation.Verdict

/**
 * How the person has the threads on screen: which are folded open or shut against their state's default, which guest
 * notes they dismissed, which they adopted as their own drafts, and the verdict for the staged batch.
 */
@Stable
class ThreadViews {
    var toggled by mutableStateOf(emptySet<String>())
    var dismissed by mutableStateOf(emptySet<String>())
    var adopted by mutableStateOf(emptySet<String>())

    /** The verdict chosen to send with the staged batch. */
    var verdict by mutableStateOf(Verdict.Comment)

    fun expanded(thread: ReviewThread): Boolean = (thread.state in OPEN_BY_DEFAULT) != (thread.id in toggled)

    fun toggle(thread: ReviewThread) {
        toggled = if (thread.id in toggled) toggled - thread.id else toggled + thread.id
    }

    /** [threads] as shown: dismissed guest notes left out, adopted ones as drafts. */
    fun shown(threads: List<ReviewThread>): List<ReviewThread> = threads
        .filter { it.id !in dismissed }
        .map { if (it.id in adopted) it.copy(state = ThreadState.Draft, messages = it.messages.map { m -> m.copy(guest = false) }) else it }

    private companion object {
        val OPEN_BY_DEFAULT = setOf(ThreadState.Open, ThreadState.Draft, ThreadState.Guest)
    }
}
