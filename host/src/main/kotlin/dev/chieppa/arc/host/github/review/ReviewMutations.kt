package dev.chieppa.arc.host.github.review

/** The mutations Arc sends to GitHub's GraphQL API, and the queries a push makes between them. */
object ReviewMutations {
    val ADD_REACTION = GraphQlMutation("addReaction", "AddReactionInput")
    val REMOVE_REACTION = GraphQlMutation("removeReaction", "RemoveReactionInput")
    val RESOLVE_THREAD = GraphQlMutation("resolveReviewThread", "ResolveReviewThreadInput")
    val UNRESOLVE_THREAD = GraphQlMutation("unresolveReviewThread", "UnresolveReviewThreadInput")
    val ADD_REVIEW = GraphQlMutation("addPullRequestReview", "AddPullRequestReviewInput", "pullRequestReview { id }")
    val ADD_THREAD = GraphQlMutation("addPullRequestReviewThread", "AddPullRequestReviewThreadInput", "thread { id }")
    val ADD_REPLY = GraphQlMutation("addPullRequestReviewThreadReply", "AddPullRequestReviewThreadReplyInput", "comment { id }")
    val SUBMIT_REVIEW = GraphQlMutation("submitPullRequestReview", "SubmitPullRequestReviewInput", "pullRequestReview { id state }")
    val ADD_COMMENT = GraphQlMutation("addComment", "AddCommentInput", "commentEdge { node { id } }")
    val MARK_VIEWED = GraphQlMutation("markFileAsViewed", "MarkFileAsViewedInput")

    /** The variable naming the pull request in [PENDING_REVIEW]. */
    const val PULL = "pull"

    /** The person's pending review on a pull request, by the pull request's node id. */
    const val PENDING_REVIEW = "query(\$$PULL: ID!) { node(id: \$$PULL) { ... on PullRequest { " +
        "reviews(states: [PENDING], first: 1) { nodes { id viewerDidAuthor } } } } }"
}
