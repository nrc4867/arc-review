package dev.chieppa.arc.ui.services

import dev.chieppa.arc.core.conversation.PushOutcome
import dev.chieppa.arc.core.conversation.Reaction
import dev.chieppa.arc.core.conversation.ReviewPush
import dev.chieppa.arc.core.source.Source

/**
 * What the window asks of the host about a review's conversation. Reading happens whenever asked; each write is one
 * the person made deliberately: a reaction, resolving a thread, or a confirmed push. Every call runs off the UI thread.
 */
interface ConversationService {
    /** The conversation on pull request [source], or null when it has no host to ask or the host gives no answer. */
    suspend fun conversation(source: Source.PullRequest): HostedConversation?

    /** Leaves or takes away the person's [reaction] on the host's message [subjectId]; false when the host refused. */
    suspend fun react(source: Source, subjectId: String, reaction: Reaction, add: Boolean): Boolean

    /** Resolves the thread [threadId], or opens it again; false when the host refused. */
    suspend fun resolve(source: Source, threadId: String, resolved: Boolean): Boolean

    /** Sends [push] to the host of its source, with [hosted] as read last for a pull request. */
    suspend fun push(push: ReviewPush, hosted: HostedConversation?): PushOutcome

    /** The picture at [url], from the cache while it is fresh; null when there is none to be had. */
    suspend fun avatar(url: String): ByteArray?

    /** The picture at [url] in a comment; null when it is not a picture or cannot be had. */
    suspend fun picture(url: String): ByteArray?
}
