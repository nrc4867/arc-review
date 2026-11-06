package dev.chieppa.arc.core.conversation

import dev.chieppa.arc.core.source.Source

/**
 * A thread on one line of a file in a source. [changedIn] is the short sha of the revision that changed its lines,
 * for an addressed thread; [link] names what the thread ties to elsewhere, such as a section of a design doc.
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
)
