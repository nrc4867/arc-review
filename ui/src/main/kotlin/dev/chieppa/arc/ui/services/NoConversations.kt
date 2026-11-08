package dev.chieppa.arc.ui.services

import dev.chieppa.arc.core.conversation.PushOutcome
import dev.chieppa.arc.core.conversation.Reaction
import dev.chieppa.arc.core.conversation.ReviewPush
import dev.chieppa.arc.core.source.Source

/** No host to ask: no conversation is read and nothing is sent, so every push fails every comment. */
object NoConversations : ConversationService {
    private const val NO_HOST = "No host"

    override suspend fun conversation(source: Source.PullRequest): HostedConversation? = null

    override suspend fun react(source: Source, subjectId: String, reaction: Reaction, add: Boolean): Boolean = false

    override suspend fun resolve(source: Source, threadId: String, resolved: Boolean): Boolean = false

    override suspend fun push(push: ReviewPush, hosted: HostedConversation?): PushOutcome =
        PushOutcome(failed = push.items.associate { it.comment.id to NO_HOST })

    override suspend fun avatar(url: String): ByteArray? = null

    override suspend fun picture(url: String): ByteArray? = null
}
