package dev.chieppa.arc.core.conversation

import dev.chieppa.arc.core.source.Source

/** The person's own comments and notes on a file, drawn as threads under their lines. */
object LocalThreads {
    /**
     * The drafts, staged comments and notes the person wrote on [path] of [source] that are not replies, each as a
     * thread at the line [placed] gives it now. One whose line is gone is left out here: the staged list shows it.
     * [author] names the person on their own messages.
     */
    fun on(local: LocalConversation, source: Source, path: String, author: String, placed: (LocalComment) -> Int?): List<ReviewThread> =
        local.of(source).filter { it.anchor?.path == path && !it.isReply }.mapNotNull { comment ->
            val anchor = comment.anchor ?: return@mapNotNull null
            val line = placed(comment) ?: return@mapNotNull null
            ReviewThread(
                id = comment.id,
                source = source,
                path = path,
                line = line,
                side = anchor.side,
                state = stateOf(comment.kind),
                messages = listOf(ThreadMessage(author, comment.body, comment.writtenAt, id = comment.id)),
                madeOn = anchor.madeOn,
                startLine = anchor.startLine?.let { line - (anchor.line - it) },
                local = comment,
            )
        }

    /** [threads] with the person's replies to each attached to it. */
    fun withReplies(threads: List<ReviewThread>, local: LocalConversation): List<ReviewThread> {
        val replies = local.comments.filter { it.isReply }.groupBy { it.inReplyTo }
        return threads.map { thread -> replies[thread.id]?.let { thread.copy(replies = it) } ?: thread }
    }

    fun stateOf(kind: LocalKind): ThreadState = when (kind) {
        LocalKind.Draft -> ThreadState.Draft
        LocalKind.Staged -> ThreadState.Staged
        LocalKind.Note -> ThreadState.Note
    }
}
