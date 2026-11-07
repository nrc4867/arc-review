package dev.chieppa.arc.ui.switcher

import dev.chieppa.arc.core.source.RepoName
import dev.chieppa.arc.git.repo.LocalBranch
import dev.chieppa.arc.host.github.pulls.PullRequestSummary
import dev.chieppa.arc.host.github.repo.GitHubRepo
import dev.chieppa.arc.ui.services.ReviewData
import java.util.concurrent.ConcurrentHashMap

/**
 * Asks [data] what followed repos hold, keeping each answer for [SwitcherLimits.CACHE_MILLIS] so typing back and forth
 * asks GitHub once per query: open pull requests by title, in one search across the repos, and each clone's branches.
 */
class FollowedLookup(private val data: ReviewData, private val clock: () -> Long = System::currentTimeMillis) {
    private class Kept<T>(val at: Long, val value: T)

    private val pulls = ConcurrentHashMap<String, Kept<List<PullRequestSummary>?>>()
    private val branches = ConcurrentHashMap<RepoName, Kept<List<LocalBranch>>>()

    /** The open pull requests in [repos] whose titles hold [text]; null when GitHub cannot be asked. */
    suspend fun pulls(repos: Collection<GitHubRepo>, text: String): List<PullRequestSummary>? {
        val words = text.trim()
        if (repos.isEmpty() || words.isEmpty()) return emptyList()
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
