package dev.chieppa.arc.host.github

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

/**
 * What Arc reads from GitHub, read-only: who the person is, the pull requests a set of searches finds, one pull
 * request and the heads it has had, the checks on commits, and the person's notifications. Every call throws [GitHubException] when GitHub gives nothing to use.
 */
class GitHub(private val client: GitHubClient = GitHubClient()) {
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
}
