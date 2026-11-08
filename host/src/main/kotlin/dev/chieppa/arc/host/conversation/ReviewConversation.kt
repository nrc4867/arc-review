package dev.chieppa.arc.host.conversation

import dev.chieppa.arc.core.change.ObjectId
import dev.chieppa.arc.core.conversation.ReviewThread
import dev.chieppa.arc.core.conversation.ThreadContext
import dev.chieppa.arc.core.source.Source

/**
 * Threads on lines from outside the host and the person's own writing, such as an outside agent's guest notes or the
 * dev-only samples.
 */
interface ReviewConversation {
    /** The threads on [path] in [source], placed against [context]. */
    fun threads(source: Source, path: String, context: ThreadContext): List<ReviewThread>

    /**
     * The threads on [paths] of [source] that were started on one of its [heads] (oldest first), each naming that
     * head in [ReviewThread.madeOn], for counting them by revision.
     */
    fun revisionThreads(source: Source, paths: List<String>, heads: List<ObjectId>): List<ReviewThread> = emptyList()
}
