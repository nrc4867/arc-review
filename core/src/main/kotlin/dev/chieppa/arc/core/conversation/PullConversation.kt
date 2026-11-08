package dev.chieppa.arc.core.conversation

/**
 * What a host holds of a pull request's conversation: its [description] as written by its author, the [entries] after
 * it oldest first, the [threads] on its lines, and the comments of the review the person started there and has not
 * submitted ([pending]). [viewer] is the person's login on the host.
 */
data class PullConversation(
    val description: ThreadMessage? = null,
    val entries: List<ConversationEntry> = emptyList(),
    val threads: List<ReviewThread> = emptyList(),
    val pending: List<LocalComment> = emptyList(),
    val viewer: String? = null,
)
