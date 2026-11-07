package dev.chieppa.arc.host.github.pulls

import dev.chieppa.arc.host.github.repo.GitHubRepo
import java.time.Instant

/**
 * A pull request as GitHub reports it: what a list of them shows, and the facts its "waiting on" is worked out from.
 * [reviewDecision] and [mergeable] are GitHub's enum names, such as `APPROVED` and `MERGEABLE`; [lastCommitAt] is when
 * its head commit was made; [unresolvedThreads] counts the review threads not resolved yet.
 */
data class PullRequestSummary(
    val repo: GitHubRepo,
    val number: Int,
    val title: String,
    val url: String,
    val author: String?,
    val draft: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant,
    val labels: List<PullLabel> = emptyList(),
    val comments: Int = 0,
    val checks: CheckRollup = CheckRollup.NONE,
    val reviewDecision: String? = null,
    val mergeable: String? = null,
    val requests: List<ReviewRequest> = emptyList(),
    val latestReviews: List<PullReview> = emptyList(),
    val unresolvedThreads: Int = 0,
    val lastCommitAt: Instant? = null,
    val baseBranch: String? = null,
    val headOid: String? = null,
    val baseOid: String? = null,
) {
    /** What names it across repos: `owner/name#412`. */
    val key: String get() = "${repo.fullName}#$number"
}
