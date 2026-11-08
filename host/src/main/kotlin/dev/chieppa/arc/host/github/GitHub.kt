package dev.chieppa.arc.host.github

import dev.chieppa.arc.core.conversation.PushOutcome
import dev.chieppa.arc.core.conversation.Reaction
import dev.chieppa.arc.core.conversation.ReviewPush
import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.host.github.api.GitHubClient
import dev.chieppa.arc.host.github.api.GitHubException
import dev.chieppa.arc.host.github.history.CommitChecksQuery
import dev.chieppa.arc.host.github.history.PullHistory
import dev.chieppa.arc.host.github.history.PullHistoryQuery
import dev.chieppa.arc.host.github.notifications.NotificationsFeed
import dev.chieppa.arc.host.github.pulls.CheckState
import dev.chieppa.arc.host.github.pulls.OpenPullSearch
import dev.chieppa.arc.host.github.pulls.PullRequestQuery
import dev.chieppa.arc.host.github.pulls.PullRequestSummary
import dev.chieppa.arc.host.github.pulls.SearchDocument
import dev.chieppa.arc.host.github.pulls.SearchPoll
import dev.chieppa.arc.host.github.pulls.Viewer
import dev.chieppa.arc.host.github.pulls.ViewerQuery
import dev.chieppa.arc.host.github.repo.GitHubRepo
import dev.chieppa.arc.host.github.review.CommitCommenter
import dev.chieppa.arc.host.github.review.ConversationParser
import dev.chieppa.arc.host.github.review.ConversationQuery
import dev.chieppa.arc.host.github.review.HostConversation
import dev.chieppa.arc.host.github.review.MutationPacer
import dev.chieppa.arc.host.github.review.ReviewInputs
import dev.chieppa.arc.host.github.review.ReviewMutations
import dev.chieppa.arc.host.github.review.ReviewPublisher
import dev.chieppa.arc.host.github.review.ReviewWriter

/**
 * What Arc reads from and writes to GitHub. It reads who the person is, the pull requests a set of searches finds, one
 * pull request, the heads it has had and its conversation, the checks on commits, and the person's notifications. It
 * writes only when the person acts: reactions, resolving threads, and pushing a staged batch as a review or as commit
 * comments, every write spaced by [pacer]. Every call throws [GitHubException] when GitHub gives nothing to use.
 */
class GitHub(private val client: GitHubClient = GitHubClient(), pacer: MutationPacer = MutationPacer()) {
    private val writer = ReviewWriter(client, pacer)
    private val publisher = ReviewPublisher(writer)
    private val commenter = CommitCommenter(client, pacer)

    /** The person's notifications, polled conditionally. */
    val notifications = NotificationsFeed(client)

    /** Whether a token is to hand. */
    fun hasToken(): Boolean = client.hasToken()

    /** The person signed in, with their teams. */
    suspend fun viewer(): Viewer {
        val login = ViewerQuery.login(client.graphql(ViewerQuery.LOGIN))
            ?: throw GitHubException(GitHubException.Reason.Failed, "No viewer")
        return Viewer(login, ViewerQuery.teams(client.graphql(ViewerQuery.teams(login))))
    }

    /** Every search in [searches], by key, in one request. */
    suspend fun search(searches: Map<String, String>): SearchPoll {
        val document = SearchDocument(searches)
        return SearchPoll.parse(document, client.graphql(document.text))
    }

    /** The open pull requests in [repos] whose titles hold [text], in one search; none for no repo or no text. */
    suspend fun openPulls(repos: Collection<GitHubRepo>, text: String): List<PullRequestSummary> {
        val query = OpenPullSearch.query(repos, text) ?: return emptyList()
        return search(mapOf(OpenPullSearch.KEY to query)).results[OpenPullSearch.KEY]?.pulls.orEmpty()
    }

    /** Pull request [number] of [repo], or null when there is none. */
    suspend fun pullRequest(repo: GitHubRepo, number: Int): PullRequestSummary? =
        PullRequestQuery.parse(client.graphql(PullRequestQuery.text(repo, number)))

    /** What GitHub knows of the heads pull request [number] of [repo] has had, or null when there is no such pull request. */
    suspend fun pullHistory(repo: GitHubRepo, number: Int): PullHistory? =
        PullHistoryQuery.parse(client.graphql(PullHistoryQuery.text(repo, number)))

    /** The overall checks on each of [oids] in [repo] that has any, in one request. */
    suspend fun commitChecks(repo: GitHubRepo, oids: List<String>): Map<String, CheckState> =
        if (oids.isEmpty()) emptyMap() else CommitChecksQuery.parse(oids, client.graphql(CommitChecksQuery.text(repo, oids)))

    /** The conversation of pull request [source] in [repo], with the ids writing to it needs, or null when there is no such pull request. */
    suspend fun conversation(repo: GitHubRepo, source: Source.PullRequest): HostConversation? =
        ConversationParser.parse(client.graphql(ConversationQuery.TEXT, ConversationQuery.variables(repo, source.number)), source)

    /** Adds the person's [reaction] to the comment, review or pull request [subjectId], or takes it away when not [add]. */
    suspend fun react(subjectId: String, reaction: Reaction, add: Boolean) {
        writer.mutate(if (add) ReviewMutations.ADD_REACTION else ReviewMutations.REMOVE_REACTION, ReviewInputs.reaction(subjectId, reaction))
    }

    /** Resolves review thread [threadId], or opens it again when not [resolved]. */
    suspend fun resolve(threadId: String, resolved: Boolean) {
        writer.mutate(if (resolved) ReviewMutations.RESOLVE_THREAD else ReviewMutations.UNRESOLVE_THREAD, ReviewInputs.thread(threadId))
    }

    /** Pushes [push] to the pull request with node id [pullId] in [repo] as one review; see [ReviewPublisher]. */
    suspend fun pushReview(repo: GitHubRepo, pullId: String, push: ReviewPush): PushOutcome = publisher.push(repo, pullId, push)

    /** Pushes [push] for a source that is not a pull request as comments on its head commit in [repo]. */
    suspend fun commitComments(repo: GitHubRepo, push: ReviewPush): PushOutcome = commenter.push(repo, push)
}
