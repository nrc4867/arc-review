package dev.chieppa.arc.host.github

import dev.chieppa.arc.host.github.api.GitHubClient
import dev.chieppa.arc.host.github.api.GitHubException
import dev.chieppa.arc.host.github.notifications.NotificationsFeed
import dev.chieppa.arc.host.github.pulls.PullRequestQuery
import dev.chieppa.arc.host.github.pulls.PullRequestSummary
import dev.chieppa.arc.host.github.pulls.SearchDocument
import dev.chieppa.arc.host.github.pulls.SearchPoll
import dev.chieppa.arc.host.github.pulls.Viewer
import dev.chieppa.arc.host.github.pulls.ViewerQuery
import dev.chieppa.arc.host.github.repo.GitHubRepo

/**
 * What Arc reads from GitHub, read-only: who the person is, the pull requests a set of searches finds, one pull
 * request, and the person's notifications. Every call throws [GitHubException] when GitHub gives nothing to use.
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

    /** Pull request [number] of [repo], or null when there is none. */
    suspend fun pullRequest(repo: GitHubRepo, number: Int): PullRequestSummary? =
        PullRequestQuery.parse(client.graphql(PullRequestQuery.text(repo, number)))
}
