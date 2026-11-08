package dev.chieppa.arc.host.github.review

import dev.chieppa.arc.core.conversation.PullConversation

/**
 * A pull request's [conversation] as GitHub holds it, with what writing to it needs: the pull request's node id
 * ([pullId]), its head commit ([headOid]) and the node id of the person's pending review there ([pendingReviewId]).
 */
data class HostConversation(
    val pullId: String,
    val headOid: String?,
    val pendingReviewId: String?,
    val conversation: PullConversation,
)
