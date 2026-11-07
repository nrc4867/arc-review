package dev.chieppa.arc.host.github.pulls

/** The GraphQL fragment every pull request query asks for, read by [PullRequestParser]. */
object PullRequestFragment {
    const val NAME = "Pull"

    val TEXT = """
        fragment $NAME on PullRequest {
          number title url isDraft createdAt updatedAt baseRefName headRefOid baseRefOid
          repository { nameWithOwner }
          author { login }
          labels(first: 10) { nodes { name color } }
          totalCommentsCount
          reviewDecision mergeable
          reviewRequests(first: 10) { nodes { requestedReviewer { __typename ... on User { login } ... on Team { slug organization { login } } } } }
          latestReviews(first: 10) { nodes { author { login } state submittedAt } }
          reviewThreads(first: 50) { nodes { isResolved } }
          commits(last: 1) { nodes { commit { committedDate statusCheckRollup { state contexts(first: 30) { nodes { __typename ... on CheckRun { name conclusion status } ... on StatusContext { context state } } } } } } }
        }
    """.trimIndent()
}
