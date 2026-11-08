package dev.chieppa.arc.host.github.review

import dev.chieppa.arc.host.github.repo.GitHubRepo
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

/** The GraphQL document that reads a pull request's whole conversation, with the variables naming the pull request. */
object ConversationQuery {
    /** The most issue comments, reviews and threads read, each oldest first. */
    const val ENTRIES = 100

    /** The most comments read in each thread. */
    const val THREAD_COMMENTS = 100

    private const val AUTHOR = "author { login avatarUrl }"
    private const val REACTIONS = "reactionGroups { content viewerHasReacted reactors { totalCount } }"

    const val TEXT = "query(\$owner: String!, \$name: String!, \$number: Int!) { viewer { login } " +
        "repository(owner: \$owner, name: \$name) { pullRequest(number: \$number) { " +
        "id headRefOid body createdAt $AUTHOR $REACTIONS " +
        "comments(first: $ENTRIES) { nodes { id body createdAt $AUTHOR $REACTIONS } } " +
        "reviews(first: $ENTRIES) { nodes { id body state createdAt submittedAt $AUTHOR comments { totalCount } $REACTIONS } } " +
        "pending: reviews(states: [PENDING], first: 1) { nodes { id viewerDidAuthor } } " +
        "reviewThreads(first: $ENTRIES) { nodes { id path line originalLine startLine originalStartLine diffSide " +
        "isResolved isOutdated subjectType comments(first: $THREAD_COMMENTS) { nodes { id body createdAt state viewerDidAuthor " +
        "line originalLine startLine originalStartLine replyTo { id } pullRequestReview { id } commit { oid } originalCommit { oid } $AUTHOR $REACTIONS } } } } } } }"

    private const val OWNER = "owner"
    private const val NAME = "name"
    private const val NUMBER = "number"

    fun variables(repo: GitHubRepo, number: Int): JsonObject = buildJsonObject {
        put(OWNER, JsonPrimitive(repo.owner))
        put(NAME, JsonPrimitive(repo.name))
        put(NUMBER, JsonPrimitive(number))
    }
}
