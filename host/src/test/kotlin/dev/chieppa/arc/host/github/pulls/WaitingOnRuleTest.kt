package dev.chieppa.arc.host.github.pulls

import dev.chieppa.arc.host.github.repo.GitHubRepo
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

class WaitingOnRuleTest {
    private val me = Viewer("nico", setOf(ReviewRequest.Team("lumen", "auth")))
    private val monday = Instant.parse("2026-11-02T10:00:00Z")
    private val tuesday = Instant.parse("2026-11-03T10:00:00Z")
    private val pull = PullRequestSummary(GitHubRepo("lumen", "api"), 412, "Rotate", "", "r-okafor", false, monday, tuesday)

    private fun waiting(pull: PullRequestSummary) = WaitingOnRule.of(pull, me)

    @Test
    fun askedDirectlyOrThroughATeamIsYou() {
        assertEquals(WaitingOn.You(false), waiting(pull.copy(requests = listOf(ReviewRequest.User("nico")))))
        assertEquals(WaitingOn.You(false), waiting(pull.copy(requests = listOf(ReviewRequest.Team("lumen", "auth")))))
    }

    @Test
    fun askedAgainAfterNewCommitsIsAReReview() {
        val reviewed = pull.copy(
            requests = listOf(ReviewRequest.User("nico")),
            latestReviews = listOf(PullReview("nico", "CHANGES_REQUESTED", monday)),
            lastCommitAt = tuesday,
        )
        assertEquals(WaitingOn.You(true), waiting(reviewed))
        assertEquals(WaitingOn.You(true), waiting(reviewed.copy(requests = emptyList())))
    }

    @Test
    fun anApprovalWithNewCommitsIsNotWaitingOnYou() {
        val approved = pull.copy(latestReviews = listOf(PullReview("nico", "APPROVED", monday)), lastCommitAt = tuesday)
        assertEquals(WaitingOn.Reviewers(0), waiting(approved))
    }

    @Test
    fun failedChecksComeBeforeTheAuthorsThreads() {
        val failing = pull.copy(checks = CheckRollup(CheckState.Failed, listOf("test")), unresolvedThreads = 2)
        assertEquals(WaitingOn.Ci("test"), waiting(failing))
        assertEquals(WaitingOn.Author(2), waiting(failing.copy(checks = CheckRollup(CheckState.Passed))))
        assertEquals(WaitingOn.Author(0), waiting(pull.copy(reviewDecision = "CHANGES_REQUESTED")))
    }

    @Test
    fun approvedAndMergeableWaitsOnAMerge() {
        assertEquals(WaitingOn.Merge, waiting(pull.copy(reviewDecision = "APPROVED", mergeable = "MERGEABLE")))
        assertEquals(WaitingOn.Reviewers(0), waiting(pull.copy(reviewDecision = "APPROVED", mergeable = "CONFLICTING")))
    }

    @Test
    fun otherwiseItWaitsOnTheReviewersAsked() {
        assertEquals(WaitingOn.Reviewers(2), waiting(pull.copy(requests = listOf(ReviewRequest.User("a"), ReviewRequest.User("b")))))
    }

    @Test
    fun yourOwnPullRequestNeverWaitsOnYou() {
        val mine = pull.copy(author = "nico", requests = listOf(ReviewRequest.Team("lumen", "auth")))
        assertEquals(WaitingOn.Reviewers(1), waiting(mine))
    }
}
