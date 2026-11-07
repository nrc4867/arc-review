package dev.chieppa.arc.ui.services

import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.git.repo.GitRepository
import dev.chieppa.arc.git.source.PullRequestLookup
import dev.chieppa.arc.git.source.PullRequestRefs
import dev.chieppa.arc.host.github.GitHub
import dev.chieppa.arc.host.github.api.GitHubException
import dev.chieppa.arc.host.github.pulls.PullRequestSummary
import dev.chieppa.arc.host.github.repo.GitHubRemotes
import dev.chieppa.arc.host.github.repo.GitHubRepo
import java.util.concurrent.ConcurrentHashMap

/**
 * Pull requests of local repos, asked of [github] by the GitHub repo each clone's `origin` points at. What GitHub said
 * last of each is kept, so the source chip and the reader ask once per read. A repo without a GitHub origin, or no
 * token, knows no pull request.
 */
class GitHubPulls(private val github: GitHub) : PullRequestLookup {
    private val last = ConcurrentHashMap<Source.PullRequest, PullRequestSummary>()

    override suspend fun refs(repo: GitRepository, source: Source.PullRequest): PullRequestRefs? {
        val pull = fetch(repo, source) ?: return null
        val base = pull.baseBranch ?: return null
        return PullRequestRefs(base, pull.headOid, pull.baseOid, pull.title)
    }

    /** What GitHub said of [source] when it was last read, if it was. */
    fun known(source: Source.PullRequest): PullRequestSummary? = last[source]

    /** The GitHub repo [repo]'s `origin` points at, if it is one. */
    suspend fun gitHubRepo(repo: GitRepository): GitHubRepo? = repo.remoteUrl()?.let(GitHubRemotes::parse)

    private suspend fun fetch(repo: GitRepository, source: Source.PullRequest): PullRequestSummary? {
        val remote = gitHubRepo(repo) ?: return null
        val pull = try {
            github.pullRequest(remote, source.number)
        } catch (_: GitHubException) {
            null
        } ?: return null
        last[source] = pull
        return pull
    }
}
