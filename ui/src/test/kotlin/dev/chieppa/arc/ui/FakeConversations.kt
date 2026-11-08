package dev.chieppa.arc.ui

import dev.chieppa.arc.core.conversation.PushOutcome
import dev.chieppa.arc.core.conversation.Reaction
import dev.chieppa.arc.core.conversation.ReviewPush
import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.ui.services.ConversationService
import dev.chieppa.arc.ui.services.HostedConversation
import kotlinx.coroutines.CompletableDeferred

/**
 * A host that answers from what the test sets and keeps every write it was asked for. While [answer] is set, writes
 * wait for the test to complete it, so a test can see what the window shows before the host has answered.
 */
class FakeConversations : ConversationService {
    var hosted: HostedConversation? = null
    var accepts = true
    var outcome: (ReviewPush) -> PushOutcome = { push -> PushOutcome(sent = push.items.map { it.comment.id }.toSet(), submitted = true) }
    val reactions = mutableListOf<Triple<String, Reaction, Boolean>>()
    val resolved = mutableListOf<Pair<String, Boolean>>()
    val pushes = mutableListOf<ReviewPush>()
    var answer: CompletableDeferred<Unit>? = null

    override suspend fun conversation(source: Source.PullRequest): HostedConversation? = hosted

    override suspend fun react(source: Source, subjectId: String, reaction: Reaction, add: Boolean): Boolean {
        reactions += Triple(subjectId, reaction, add)
        answer?.await()
        return accepts
    }

    override suspend fun resolve(source: Source, threadId: String, resolved: Boolean): Boolean {
        this.resolved += threadId to resolved
        answer?.await()
        return accepts
    }

    override suspend fun push(push: ReviewPush, hosted: HostedConversation?): PushOutcome {
        pushes += push
        return outcome(push)
    }

    override suspend fun avatar(url: String): ByteArray? = null
}
