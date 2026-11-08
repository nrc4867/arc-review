package dev.chieppa.arc.core.conversation

/** A pull request's conversation as the person changes it on the host, shown at once and put back if the host refuses. */
object ConversationEdits {
    /** [conversation] with the person's [reaction] toggled on the message [messageId], wherever it is. */
    fun toggleReaction(conversation: PullConversation, messageId: String, reaction: Reaction): PullConversation {
        fun ThreadMessage.toggled() = if (id == messageId) copy(reactions = Reactions.toggle(reactions, reaction)) else this
        return conversation.copy(
            description = conversation.description?.toggled(),
            entries = conversation.entries.map { entry ->
                when (entry) {
                    is ConversationEntry.Comment -> entry.copy(message = entry.message.toggled())
                    is ConversationEntry.Review -> entry.copy(message = entry.message.toggled())
                }
            },
            threads = conversation.threads.map { thread -> thread.copy(messages = thread.messages.map { it.toggled() }) },
        )
    }

    /** [conversation] with the thread [threadId] resolved or opened again. */
    fun resolve(conversation: PullConversation, threadId: String, resolved: Boolean): PullConversation = conversation.copy(
        threads = conversation.threads.map { thread ->
            if (thread.id != threadId) thread else thread.copy(state = if (resolved) ThreadState.Resolved else ThreadState.Open)
        },
    )

    /** The message [messageId] in [conversation], wherever it is. */
    fun message(conversation: PullConversation, messageId: String): ThreadMessage? =
        (listOfNotNull(conversation.description) + conversation.entries.map { it.message } + conversation.threads.flatMap { it.messages })
            .firstOrNull { it.id == messageId }
}
