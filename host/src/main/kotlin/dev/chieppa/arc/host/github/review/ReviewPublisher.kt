package dev.chieppa.arc.host.github.review

import dev.chieppa.arc.core.conversation.PushItem
import dev.chieppa.arc.core.conversation.PushOutcome
import dev.chieppa.arc.core.conversation.ReviewPush
import dev.chieppa.arc.core.conversation.StagedRoute
import dev.chieppa.arc.host.github.api.GitHubException
import dev.chieppa.arc.host.github.api.JsonFields.obj
import dev.chieppa.arc.host.github.api.JsonFields.string
import dev.chieppa.arc.host.github.repo.GitHubRepo

/**
 * Pushes a staged batch to a pull request as one review. The person's pending review is reused, or a pending one is
 * started on the push's head; each line, file and reply comment is added to it one at a time, comments routed to the
 * summary join its text, and the review is submitted with the verdict. Conversation comments follow, then the files
 * marked viewed. A comment that fails is recorded against its id and the rest still go; comments read back from the
 * pending review are already on it and count as sent.
 */
class ReviewPublisher(private val writer: ReviewWriter) {
    suspend fun push(repo: GitHubRepo, pullId: String, push: ReviewPush): PushOutcome {
        val head = push.head?.value
        val reviewId = try {
            writer.pendingReview(pullId) ?: startReview(pullId, head)
        } catch (e: GitHubException) {
            return PushOutcome(failed = push.items.associate { it.comment.id to reason(e) })
        }
        val sent = linkedSetOf<String>()
        val failed = linkedMapOf<String, String>()
        val (onHost, waiting) = push.items.partition { it.comment.fromPending }
        onHost.mapTo(sent) { it.comment.id }

        for (item in waiting.filter { it.route in REVIEW_ROUTES }) {
            attempt(item, sent, failed) { addToReview(repo, head, reviewId, item) }
        }
        val summaryItems = waiting.filter { it.route == StagedRoute.Summary }
        val body = ReviewBodies.summary(repo, head, push.summary.body, summaryItems)
        val submitted = try {
            writer.mutate(ReviewMutations.SUBMIT_REVIEW, ReviewInputs.submit(reviewId, push.summary.verdict, body))
            summaryItems.mapTo(sent) { it.comment.id }
            true
        } catch (e: GitHubException) {
            summaryItems.forEach { failed[it.comment.id] = reason(e) }
            false
        }
        for (item in waiting.filter { it.route == StagedRoute.Conversation }) {
            attempt(item, sent, failed) { writer.mutate(ReviewMutations.ADD_COMMENT, ReviewInputs.comment(pullId, item.comment.body)) }
        }
        waiting.filter { it.route == StagedRoute.Commit }.forEach { failed[it.comment.id] = NOT_ON_PULL }
        return PushOutcome(sent, failed, submitted, markViewed(pullId, push.viewed))
    }

    private suspend fun startReview(pullId: String, head: String?): String =
        writer.mutate(ReviewMutations.ADD_REVIEW, ReviewInputs.review(pullId, head))
            .obj(ReviewMutations.ADD_REVIEW.field).obj("pullRequestReview").string("id")
            ?: throw GitHubException(GitHubException.Reason.Failed, NO_REVIEW)

    private suspend fun addToReview(repo: GitHubRepo, head: String?, reviewId: String, item: PushItem) {
        val body = item.comment.body
        when (item.route) {
            StagedRoute.Line -> {
                val anchor = item.anchor ?: throw GitHubException(GitHubException.Reason.Failed, NO_ANCHOR)
                writer.mutate(ReviewMutations.ADD_THREAD, ReviewInputs.lineThread(reviewId, anchor, body))
            }
            StagedRoute.File -> {
                val anchor = item.anchor ?: throw GitHubException(GitHubException.Reason.Failed, NO_ANCHOR)
                writer.mutate(ReviewMutations.ADD_THREAD, ReviewInputs.fileThread(reviewId, anchor.path, ReviewBodies.file(repo, head, anchor, body)))
            }
            else -> {
                val thread = item.comment.inReplyTo ?: throw GitHubException(GitHubException.Reason.Failed, NO_THREAD)
                writer.mutate(ReviewMutations.ADD_REPLY, ReviewInputs.reply(reviewId, thread, body))
            }
        }
    }

    private suspend fun attempt(item: PushItem, sent: MutableSet<String>, failed: MutableMap<String, String>, send: suspend () -> Unit) {
        try {
            send()
            sent += item.comment.id
        } catch (e: GitHubException) {
            failed[item.comment.id] = reason(e)
        }
    }

    /** The paths GitHub marked viewed; a path it refuses is left out. */
    private suspend fun markViewed(pullId: String, paths: List<String>): List<String> = paths.filter { path ->
        try {
            writer.mutate(ReviewMutations.MARK_VIEWED, ReviewInputs.viewed(pullId, path))
            true
        } catch (_: GitHubException) {
            false
        }
    }

    private fun reason(e: GitHubException): String = e.message ?: e.reason.name

    private companion object {
        val REVIEW_ROUTES = setOf(StagedRoute.Line, StagedRoute.File, StagedRoute.Reply)
        const val NO_REVIEW = "No pending review"
        const val NO_ANCHOR = "No line to comment on"
        const val NO_THREAD = "No thread to reply to"
        const val NOT_ON_PULL = "A commit comment on a pull request"
    }
}
