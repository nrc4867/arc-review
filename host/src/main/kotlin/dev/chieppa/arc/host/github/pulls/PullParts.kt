package dev.chieppa.arc.host.github.pulls

import java.time.Instant

/** A label on a pull request, with its color as GitHub gives it: six hex digits, no `#`. */
data class PullLabel(val name: String, val color: String)

/** Someone asked to review a pull request: a person by login, or a team by its organization and slug. */
sealed interface ReviewRequest {
    data class User(val login: String) : ReviewRequest

    data class Team(val organization: String, val slug: String) : ReviewRequest
}

/** The latest review someone left on a pull request: its [state] is GitHub's name, such as `APPROVED`. */
data class PullReview(val author: String, val state: String, val submittedAt: Instant?)

/** Where a pull request's checks stand overall. */
enum class CheckState { Passed, Failed, Running, None }

/** A head commit's checks: their overall [state] and the names of those that failed. */
data class CheckRollup(val state: CheckState, val failed: List<String> = emptyList()) {
    companion object {
        val NONE = CheckRollup(CheckState.None)
    }
}
