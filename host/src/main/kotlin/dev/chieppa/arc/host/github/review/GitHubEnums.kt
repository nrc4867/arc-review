package dev.chieppa.arc.host.github.review

import dev.chieppa.arc.core.conversation.LineSide
import dev.chieppa.arc.core.conversation.ReviewState
import dev.chieppa.arc.core.conversation.Verdict

/** The names of GitHub's GraphQL enums that reviews read and write. */
object GitHubEnums {
    const val LEFT = "LEFT"
    const val RIGHT = "RIGHT"
    const val FILE = "FILE"
    const val PENDING = "PENDING"
    const val GHOST = "ghost"

    private val STATES = mapOf(
        "APPROVED" to ReviewState.Approved,
        "CHANGES_REQUESTED" to ReviewState.ChangesRequested,
        "COMMENTED" to ReviewState.Commented,
        "DISMISSED" to ReviewState.Dismissed,
    )

    private val EVENTS = mapOf(
        Verdict.Comment to "COMMENT",
        Verdict.Approve to "APPROVE",
        Verdict.RequestChanges to "REQUEST_CHANGES",
    )

    /** A `DiffSide` as a [LineSide]: LEFT is the old side. */
    fun side(name: String?): LineSide = if (name == LEFT) LineSide.Old else LineSide.New

    fun side(side: LineSide): String = if (side == LineSide.Old) LEFT else RIGHT

    /** A submitted review's `PullRequestReviewState`, or null for a pending one or one Arc does not know. */
    fun reviewState(name: String?): ReviewState? = name?.let(STATES::get)

    /** The `PullRequestReviewEvent` that submits [verdict]. */
    fun event(verdict: Verdict): String = EVENTS.getValue(verdict)
}
