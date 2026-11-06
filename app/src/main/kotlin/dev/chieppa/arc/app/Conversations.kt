package dev.chieppa.arc.app

import dev.chieppa.arc.host.conversation.NoConversation
import dev.chieppa.arc.host.conversation.ReviewConversation
import java.util.ServiceLoader

/**
 * Where threads and staged comments come from: a [ReviewConversation] on the classpath, which only the dev-only `demo`
 * module provides, and only when the app runs with `-Pdemo`; otherwise none.
 */
internal object Conversations {
    fun load(): ReviewConversation = ServiceLoader.load(ReviewConversation::class.java).firstOrNull() ?: NoConversation
}
