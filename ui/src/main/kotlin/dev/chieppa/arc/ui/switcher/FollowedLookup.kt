package dev.chieppa.arc.ui.switcher

import dev.chieppa.arc.core.change.CommitInfo
import dev.chieppa.arc.core.source.CommitSha
import dev.chieppa.arc.core.source.RepoName
import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.git.repo.LocalBranch
import dev.chieppa.arc.host.github.pulls.PullRequestSummary
import dev.chieppa.arc.host.github.repo.GitHubRepo
import dev.chieppa.arc.ui.services.ReviewData
import java.util.concurrent.ConcurrentHashMap

/**
 * Asks [data] what followed repos hold, keeping each answer for [SwitcherLimits.CACHE_MILLIS] so typing back and forth
 * asks GitHub once per query: open pull requests by title or the latest updated, in one search across the repos, and
 * each clone's branches and newest commits, and whether a GitHub repo exists.
 */
class FollowedLookup(private val data: ReviewData, private val clock: () -> Long = System::currentTimeMillis) {
    private class Kept<T>(val at: Long, val value: T)

    private val pulls = ConcurrentHashMap<String, Kept<List<PullRequestSummary>?>>()
    private val branches = ConcurrentHashMap<RepoName, Kept<List<LocalBranch>>>()
    private val repos = ConcurrentHashMap<GitHubRepo, Kept<Boolean?>>()
    private val commits = ConcurrentHashMap<RepoName, Kept<List<CommitInfo>>>()

    /** The newest commits on [repo]'s branches, as many as [SwitcherLimits.COMMITS]. */
    suspend fun commits(repo: RepoName): List<CommitInfo> {
        fresh(commits[repo])?.let { return it.value }
        return data.recentCommits(repo, SwitcherLimits.COMMITS).also { commits[repo] = Kept(clock(), it) }
    }

    /** The commits [query] names in any of [repos]: one commit, or a range whose ends are both in the same repo. */
    suspend fun named(query: CommitQuery, repos: Collection<RepoName>): List<SwitcherItem.FollowedCommit> = repos.mapNotNull { repo ->
        val to = data.findCommit(repo, query.to) ?: return@mapNotNull null
        val head = CommitSha(to.id.value)
        if (query.from == null) return@mapNotNull SwitcherItem.FollowedCommit(Source.Commit(repo, head), to)
        val from = data.findCommit(repo, query.from) ?: return@mapNotNull null
        SwitcherItem.FollowedCommit(Source.CommitRange(repo, CommitSha(from.id.value), head), to)
    }

    /** Whether [repo] exists on GitHub; null when GitHub cannot be asked. */
    suspend fun exists(repo: GitHubRepo): Boolean? {
        fresh(repos[repo])?.let { return it.value }
        return data.repoExists(repo).also { repos[repo] = Kept(clock(), it) }
    }

    /** The open pull requests in [repos] whose titles hold [text], or the latest updated for blank [text]; null when GitHub cannot be asked. */
    suspend fun pulls(repos: Collection<GitHubRepo>, text: String): List<PullRequestSummary>? {
        val words = text.trim()
        if (repos.isEmpty()) return emptyList()
        val key = repos.map { it.fullName.lowercase() }.sorted().joinToString(REPO_SEPARATOR) + KEY_SEPARATOR + words.lowercase()
        fresh(pulls[key])?.let { return it.value }
        return data.openPulls(repos, words).also { pulls[key] = Kept(clock(), it) }
    }

    /** The branches of [repo]'s clone. */
    suspend fun branches(repo: RepoName): List<LocalBranch> {
        fresh(branches[repo])?.let { return it.value }
        return data.branches(repo).also { branches[repo] = Kept(clock(), it) }
    }

    private fun <T> fresh(kept: Kept<T>?): Kept<T>? = kept?.takeIf { clock() - it.at < SwitcherLimits.CACHE_MILLIS }

    private companion object {
        const val REPO_SEPARATOR = " "
        const val KEY_SEPARATOR = "|"
    }
}
