package dev.chieppa.arc.core.conversation

/** One entry in a pull request's conversation, oldest first: a comment on it, or a submitted review's summary. */
sealed interface ConversationEntry {
    val message: ThreadMessage

    /** A comment on the pull request as a whole. */
    data class Comment(override val message: ThreadMessage) : ConversationEntry

    /** A submitted review: its verdict, its summary text and how many line comments it left. */
    data class Review(override val message: ThreadMessage, val state: ReviewState, val comments: Int = 0) : ConversationEntry
}
