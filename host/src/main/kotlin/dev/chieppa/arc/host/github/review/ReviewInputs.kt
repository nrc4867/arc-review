package dev.chieppa.arc.host.github.review

import dev.chieppa.arc.core.conversation.CommentAnchor
import dev.chieppa.arc.core.conversation.Reaction
import dev.chieppa.arc.core.conversation.Verdict
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

/** The `input` objects of [ReviewMutations], by GitHub's field names. */
object ReviewInputs {
    private const val SUBJECT_ID = "subjectId"
    private const val CONTENT = "content"
    private const val THREAD_ID = "threadId"
    private const val PULL_ID = "pullRequestId"
    private const val COMMIT = "commitOID"
    private const val REVIEW_ID = "pullRequestReviewId"
    private const val REVIEW_THREAD_ID = "pullRequestReviewThreadId"
    private const val PATH = "path"
    private const val LINE = "line"
    private const val SIDE = "side"
    private const val START_LINE = "startLine"
    private const val START_SIDE = "startSide"
    private const val SUBJECT_TYPE = "subjectType"
    private const val BODY = "body"
    private const val EVENT = "event"

    fun reaction(subjectId: String, reaction: Reaction): JsonObject = input {
        text(SUBJECT_ID, subjectId)
        text(CONTENT, ReactionContent.name(reaction))
    }

    fun thread(threadId: String): JsonObject = input { text(THREAD_ID, threadId) }

    /** A pending review on [pullId], on [commit] when it is known; it has no event, so it stays pending. */
    fun review(pullId: String, commit: String?): JsonObject = input {
        text(PULL_ID, pullId)
        commit?.let { text(COMMIT, it) }
    }

    /** A thread on the lines of [anchor] in pending review [reviewId]. */
    fun lineThread(reviewId: String, anchor: CommentAnchor, body: String): JsonObject = input {
        text(REVIEW_ID, reviewId)
        text(PATH, anchor.path)
        put(LINE, JsonPrimitive(anchor.line))
        text(SIDE, GitHubEnums.side(anchor.side))
        anchor.startLine?.takeIf { it != anchor.line }?.let {
            put(START_LINE, JsonPrimitive(it))
            text(START_SIDE, GitHubEnums.side(anchor.side))
        }
        text(BODY, body)
    }

    /** A thread on the whole of [path] in pending review [reviewId]. */
    fun fileThread(reviewId: String, path: String, body: String): JsonObject = input {
        text(REVIEW_ID, reviewId)
        text(PATH, path)
        text(SUBJECT_TYPE, GitHubEnums.FILE)
        text(BODY, body)
    }

    fun reply(reviewId: String, threadId: String, body: String): JsonObject = input {
        text(REVIEW_THREAD_ID, threadId)
        text(REVIEW_ID, reviewId)
        text(BODY, body)
    }

    fun submit(reviewId: String, verdict: Verdict, body: String): JsonObject = input {
        text(REVIEW_ID, reviewId)
        text(EVENT, GitHubEnums.event(verdict))
        if (body.isNotEmpty()) text(BODY, body)
    }

    fun comment(subjectId: String, body: String): JsonObject = input {
        text(SUBJECT_ID, subjectId)
        text(BODY, body)
    }

    fun viewed(pullId: String, path: String): JsonObject = input {
        text(PULL_ID, pullId)
        text(PATH, path)
    }

    private fun input(fill: JsonObjectBuilder.() -> Unit): JsonObject = buildJsonObject(fill)

    private fun JsonObjectBuilder.text(name: String, value: String) {
        put(name, JsonPrimitive(value))
    }
}
