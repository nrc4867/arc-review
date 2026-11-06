package dev.chieppa.arc.host.conversation

import dev.chieppa.arc.core.conversation.ReviewThread
import dev.chieppa.arc.core.conversation.StagedComment
import dev.chieppa.arc.core.conversation.ThreadContext
import dev.chieppa.arc.core.source.Source

/** A review with no threads and nothing staged. */
object NoConversation : ReviewConversation {
    override fun threads(source: Source, path: String, context: ThreadContext): List<ReviewThread> = emptyList()

    override fun staged(changes: Map<Source, List<String>>): List<StagedComment> = emptyList()
}
