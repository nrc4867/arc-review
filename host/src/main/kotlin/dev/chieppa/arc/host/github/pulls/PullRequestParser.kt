package dev.chieppa.arc.host.github.pulls

import dev.chieppa.arc.host.github.api.JsonFields.boolean
import dev.chieppa.arc.host.github.api.JsonFields.instant
import dev.chieppa.arc.host.github.api.JsonFields.int
import dev.chieppa.arc.host.github.api.JsonFields.nodes
import dev.chieppa.arc.host.github.api.JsonFields.obj
import dev.chieppa.arc.host.github.api.JsonFields.string
import dev.chieppa.arc.host.github.repo.GitHubRepo
import kotlinx.serialization.json.JsonObject

/** Reads a pull request answered with [PullRequestFragment]; null for anything that is not one, such as an issue. */
object PullRequestParser {
    private const val USER = "User"
    private const val TEAM = "Team"

    fun parse(node: JsonObject): PullRequestSummary? {
        val repo = node.obj("repository").string("nameWithOwner")?.let(GitHubRepo::parse) ?: return null
        val number = node.int("number") ?: return null
        val created = node.instant("createdAt") ?: return null
        val commit = node.nodes("commits").firstOrNull().obj("commit")
        return PullRequestSummary(
            repo = repo,
            number = number,
            title = node.string("title").orEmpty(),
            url = node.string("url").orEmpty(),
            author = node.obj("author").string("login"),
            draft = node.boolean("isDraft") ?: false,
            createdAt = created,
            updatedAt = node.instant("updatedAt") ?: created,
            labels = node.nodes("labels").mapNotNull { label -> label.string("name")?.let { PullLabel(it, label.string("color").orEmpty()) } },
            comments = node.int("totalCommentsCount") ?: 0,
            checks = CheckRollups.parse(commit.obj("statusCheckRollup")),
            reviewDecision = node.string("reviewDecision"),
            mergeable = node.string("mergeable"),
            requests = node.nodes("reviewRequests").mapNotNull { request(it.obj("requestedReviewer")) },
            latestReviews = node.nodes("latestReviews").mapNotNull { review ->
                review.obj("author").string("login")?.let { PullReview(it, review.string("state").orEmpty(), review.instant("submittedAt")) }
            },
            unresolvedThreads = node.nodes("reviewThreads").count { it.boolean("isResolved") == false },
            lastCommitAt = commit.instant("committedDate"),
            baseBranch = node.string("baseRefName"),
            headOid = node.string("headRefOid"),
            headBranch = node.string("headRefName"),
            baseOid = node.string("baseRefOid"),
            state = PullState.parse(node.string("state")),
        )
    }

    private fun request(reviewer: JsonObject?): ReviewRequest? = when (reviewer.string("__typename")) {
        USER -> reviewer.string("login")?.let(ReviewRequest::User)
        TEAM -> {
            val org = reviewer.obj("organization").string("login")
            val slug = reviewer.string("slug")
            if (org != null && slug != null) ReviewRequest.Team(org, slug) else null
        }
        else -> null
    }
}
