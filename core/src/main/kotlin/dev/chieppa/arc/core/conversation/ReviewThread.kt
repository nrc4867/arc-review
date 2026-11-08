package dev.chieppa.arc.core.conversation

import dev.chieppa.arc.core.change.ObjectId
import dev.chieppa.arc.core.source.Source

/**
 * A thread on one line of a file in a source, or on the lines from [startLine] to [line]. [changedIn] is the short
 * sha of the revision that changed its lines, for an addressed thread; [link] names what the thread ties to elsewhere,
 * such as a section of a design doc. [madeOn] is the source's head when the thread was started, where the host says.
 * [replies] holds the person's own replies being written or staged, which go up with the review. For the person's own
 * comment or note, [local] is what they wrote.
 */
data class ReviewThread(
    val id: String,
    val source: Source,
    val path: String,
    val line: Int,
    val side: LineSide,
    val state: ThreadState,
    val messages: List<ThreadMessage>,
    val changedIn: String? = null,
    val link: String? = null,
    val madeOn: ObjectId? = null,
    val startLine: Int? = null,
    val replies: List<LocalComment> = emptyList(),
    val local: LocalComment? = null,
) {
    /** Whether this thread is the host's, so it takes replies, resolving and reactions. */
    val hosted: Boolean get() = state in HOSTED

    private companion object {
        val HOSTED = setOf(ThreadState.Open, ThreadState.Addressed, ThreadState.Resolved)
    }
}
