package dev.chieppa.arc.host.github.review

import dev.chieppa.arc.core.conversation.ConversationEntry
import dev.chieppa.arc.core.conversation.PullConversation
import dev.chieppa.arc.core.conversation.ReviewState
import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.host.github.api.JsonFields.boolean
import dev.chieppa.arc.host.github.api.JsonFields.instant
import dev.chieppa.arc.host.github.api.JsonFields.int
import dev.chieppa.arc.host.github.api.JsonFields.nodes
import dev.chieppa.arc.host.github.api.JsonFields.obj
import dev.chieppa.arc.host.github.api.JsonFields.string
import kotlinx.serialization.json.JsonObject

/** Reads the answer to [ConversationQuery]. */
object ConversationParser {
    /** Review states that belong in the conversation even with no summary text. */
    private val VERDICTS = setOf(ReviewState.Approved, ReviewState.ChangesRequested, ReviewState.Dismissed)

    /** The conversation in [data], or null when there is no such pull request. */
    fun parse(data: JsonObject, source: Source.PullRequest): HostConversation? {
        val pull = data.obj("repository").obj("pullRequest") ?: return null
        val pullId = pull.string("id") ?: return null
        val (threads, pending) = ThreadParser.parse(pull.nodes("reviewThreads"), source)
        val conversation = PullConversation(
            description = MessageParser.parse(pull),
            entries = entries(pull),
            threads = threads,
            pending = pending,
            viewer = data.obj("viewer").string("login"),
        )
        val pendingReview = pull.nodes("pending").firstOrNull { it.boolean("viewerDidAuthor") == true }.string("id")
        return HostConversation(pullId, pull.string("headRefOid"), pendingReview, conversation)
    }

    /** The comments on the pull request and its submitted reviews that say something, oldest first. */
    private fun entries(pull: JsonObject): List<ConversationEntry> {
        val comments = pull.nodes("comments").map { ConversationEntry.Comment(MessageParser.parse(it)) }
        val reviews = pull.nodes("reviews").mapNotNull { review ->
            val state = GitHubEnums.reviewState(review.string("state")) ?: return@mapNotNull null
            if (review.string("body").isNullOrBlank() && state !in VERDICTS) return@mapNotNull null
            val message = MessageParser.parse(review, review.instant("submittedAt") ?: review.instant("createdAt"))
            ConversationEntry.Review(message, state, review.obj("comments").int("totalCount") ?: 0)
        }
        return (comments + reviews).sortedBy { it.message.writtenAt }
    }
}
