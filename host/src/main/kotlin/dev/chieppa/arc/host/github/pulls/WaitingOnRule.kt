package dev.chieppa.arc.host.github.pulls

/**
 * What a pull request waits on, first that applies:
 * 1. [WaitingOn.You] when the person or one of their teams is asked to review, or their latest review asked for
 *    changes or only commented and commits came after it; "again" when they reviewed before and commits came after.
 * 2. [WaitingOn.Ci] when the checks failed.
 * 3. [WaitingOn.Author] when review threads are open or changes were asked for.
 * 4. [WaitingOn.Merge] when it is approved and mergeable.
 * 5. [WaitingOn.Reviewers] otherwise, with how many are asked.
 */
object WaitingOnRule {
    private const val APPROVED = "APPROVED"
    private const val CHANGES_REQUESTED = "CHANGES_REQUESTED"
    private const val MERGEABLE = "MERGEABLE"
    private val REVIEWED_WITHOUT_APPROVING = setOf(CHANGES_REQUESTED, "COMMENTED")

    fun of(pull: PullRequestSummary, viewer: Viewer?): WaitingOn {
        if (viewer != null && !pull.author.equals(viewer.login, ignoreCase = true)) {
            val mine = pull.latestReviews.firstOrNull { it.author.equals(viewer.login, ignoreCase = true) }
            val newerCommits = mine?.submittedAt != null && pull.lastCommitAt != null && pull.lastCommitAt.isAfter(mine.submittedAt)
            val asked = pull.requests.any(viewer::isAsked)
            if (asked) return WaitingOn.You(again = mine != null && newerCommits)
            if (mine != null && mine.state in REVIEWED_WITHOUT_APPROVING && newerCommits) return WaitingOn.You(again = true)
        }
        if (pull.checks.state == CheckState.Failed) return WaitingOn.Ci(pull.checks.failed.firstOrNull())
        if (pull.unresolvedThreads > 0 || pull.reviewDecision == CHANGES_REQUESTED) return WaitingOn.Author(pull.unresolvedThreads)
        if (pull.reviewDecision == APPROVED && pull.mergeable == MERGEABLE) return WaitingOn.Merge
        return WaitingOn.Reviewers(pull.requests.size)
    }
}
