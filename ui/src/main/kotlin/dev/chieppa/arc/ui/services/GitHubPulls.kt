package dev.chieppa.arc.ui.services

import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.git.repo.GitRepository
import dev.chieppa.arc.git.revision.PushHistoryLookup
import dev.chieppa.arc.git.revision.PushedHead
import dev.chieppa.arc.git.source.PullRequestLookup
import dev.chieppa.arc.git.source.PullRequestRefs
import dev.chieppa.arc.host.github.GitHub
import dev.chieppa.arc.host.github.api.GitHubException
import dev.chieppa.arc.host.github.pulls.CheckState
import dev.chieppa.arc.host.github.pulls.PullRequestSummary
import dev.chieppa.arc.host.github.repo.GitHubRemotes
import dev.chieppa.arc.host.github.repo.GitHubRepo
import dev.chieppa.arc.ui.util.ArcDispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

/**
 * Pull requests of local repos, asked of [github] by the GitHub repo each clone's `origin` points at. What GitHub said
 * last of each is kept, so the source chip and the reader ask once per read. A repo without a GitHub origin, or no
 * token, knows no pull request. Blocking reads run on [dispatchers]' io.
 */
class GitHubPulls(private val github: GitHub, private val dispatchers: ArcDispatchers = ArcDispatchers.Real) : PullRequestLookup, PushHistoryLookup {
    private val last = ConcurrentHashMap<Source.PullRequest, PullRequestSummary>()

    override suspend fun refs(repo: GitRepository, source: Source.PullRequest): PullRequestRefs? {
        val pull = fetch(repo, source) ?: return null
        val base = pull.baseBranch ?: return null
        return PullRequestRefs(base, pull.headOid, pull.baseOid, pull.title)
    }

    /** What GitHub said of [source] when it was last read, if it was. */
    fun known(source: Source.PullRequest): PullRequestSummary? = last[source]

    /**
     * The heads GitHub knows [source] has had: each force push's before and after, with when the after was pushed,
     * the commits its reviews were made on, and its head now. GitHub keeps no record of an ordinary push.
     */
    override suspend fun heads(repo: GitRepository, source: Source.PullRequest): List<PushedHead> {
        val remote = gitHubRepo(repo) ?: return emptyList()
        val history = try {
            github.pullHistory(remote, source.number)
        } catch (_: GitHubException) {
            null
        } ?: return emptyList()
        return buildList {
            for (push in history.forcePushes) {
                push.before?.let { add(PushedHead(it)) }
                push.after?.let { add(PushedHead(it, push.at.epochSecond)) }
            }
            history.reviews.forEach { add(PushedHead(it)) }
            history.head?.let { add(PushedHead(it)) }
        }
    }

    /** The overall checks GitHub has on each of [oids] in [repo]'s GitHub repo; none for a repo not from GitHub. */
    suspend fun checks(repo: GitRepository, oids: List<String>): Map<String, CheckState> {
        val remote = gitHubRepo(repo) ?: return emptyMap()
        return try {
            github.commitChecks(remote, oids)
        } catch (_: GitHubException) {
            emptyMap()
        }
    }

    /** The open pull requests in [repos] whose titles hold [text]; null without a token or when GitHub gives no answer. */
    suspend fun openPulls(repos: Collection<GitHubRepo>, text: String): List<PullRequestSummary>? {
        if (!withContext(dispatchers.io) { github.hasToken() }) return null
        return try {
            github.openPulls(repos, text)
        } catch (_: GitHubException) {
            null
        }
    }

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
