package dev.chieppa.arc.core.revision

import dev.chieppa.arc.core.conversation.ThreadState

/** How many threads are [open], [addressed] and [resolved] in one place. */
data class ThreadCounts(val open: Int = 0, val addressed: Int = 0, val resolved: Int = 0) {
    val isEmpty: Boolean get() = open == 0 && addressed == 0 && resolved == 0

    /** These counts with one more thread in [state]; drafts and guest notes are not counted. */
    fun plus(state: ThreadState): ThreadCounts = when (state) {
        ThreadState.Open -> copy(open = open + 1)
        ThreadState.Addressed -> copy(addressed = addressed + 1)
        ThreadState.Resolved -> copy(resolved = resolved + 1)
        ThreadState.Draft, ThreadState.Guest -> this
    }

    companion object {
        val NONE = ThreadCounts()
    }
}
