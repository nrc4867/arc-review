package dev.chieppa.arc.host.conversation

import dev.chieppa.arc.core.conversation.ReviewThread
import dev.chieppa.arc.core.conversation.ThreadContext
import dev.chieppa.arc.core.source.Source

/** No threads from outside the host. */
object NoConversation : ReviewConversation {
    override fun threads(source: Source, path: String, context: ThreadContext): List<ReviewThread> = emptyList()
}
