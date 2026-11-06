package dev.chieppa.arc.host.conversation

import dev.chieppa.arc.core.conversation.ReviewThread
import dev.chieppa.arc.core.conversation.StagedComment
import dev.chieppa.arc.core.conversation.ThreadContext
import dev.chieppa.arc.core.source.Source

/**
 * The conversation around a review: threads on lines, from the host and the person's own drafts and guest notes, and
 * the staged comments waiting to be pushed. The local store and the host each provide one.
 */
interface ReviewConversation {
    /** The threads on [path] in [source], placed against [context]. */
    fun threads(source: Source, path: String, context: ThreadContext): List<ReviewThread>

    /** Every staged comment on [changes]' sources, placed against the files each source changes now. */
    fun staged(changes: Map<Source, List<String>>): List<StagedComment>
}
