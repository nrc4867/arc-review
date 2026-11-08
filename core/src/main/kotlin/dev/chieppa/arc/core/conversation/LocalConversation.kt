package dev.chieppa.arc.core.conversation

import dev.chieppa.arc.core.source.Source

/**
 * Everything the person has written that stays on this machine until pushed: drafts, staged comments and notes on
 * every source, and each source's verdict and summary for its next push.
 */
data class LocalConversation(
    val comments: List<LocalComment> = emptyList(),
    val summaries: Map<Source, ReviewSummary> = emptyMap(),
) {
    fun of(source: Source): List<LocalComment> = comments.filter { it.source == source }

    fun byId(id: String): LocalComment? = comments.firstOrNull { it.id == id }

    /** The staged comments on [source], in the order they were written. */
    fun staged(source: Source): List<LocalComment> = of(source).filter { it.kind == LocalKind.Staged }

    fun summary(source: Source): ReviewSummary = summaries[source] ?: ReviewSummary()

    fun withSummary(source: Source, summary: ReviewSummary): LocalConversation = copy(summaries = summaries + (source to summary))

    /** [comment] added, or put in place of the one with its id. */
    fun put(comment: LocalComment): LocalConversation =
        if (comments.any { it.id == comment.id }) copy(comments = comments.map { if (it.id == comment.id) comment else it }) else copy(comments = comments + comment)

    fun change(id: String, change: (LocalComment) -> LocalComment): LocalConversation =
        copy(comments = comments.map { if (it.id == id) change(it) else it })

    fun remove(id: String): LocalConversation = copy(comments = comments.filter { it.id != id })

    /** Every staged comment of [source] taken back to a draft. */
    fun unstageAll(source: Source): LocalConversation =
        copy(comments = comments.map { if (it.source == source && it.kind == LocalKind.Staged && !it.fromPending) it.copy(kind = LocalKind.Draft) else it })

    /**
     * What a push of [source] did: the comments it [PushOutcome.sent] are gone, those it could not send are marked
     * failed, and the summary is cleared once the review went up.
     */
    fun afterPush(source: Source, outcome: PushOutcome): LocalConversation {
        val kept = comments.filter { it.id !in outcome.sent }.map { comment ->
            if (comment.source == source && comment.kind == LocalKind.Staged) comment.copy(failed = comment.id in outcome.failed) else comment
        }
        val summary = if (outcome.submitted) summaries - source else summaries
        return LocalConversation(kept, summary)
    }

    /** The comments read back from [source]'s review pending on the host, in place of those read before. */
    fun withPending(source: Source, pending: List<LocalComment>): LocalConversation {
        val others = comments.filterNot { it.source == source && it.fromPending }
        return copy(comments = others + pending)
    }
}
