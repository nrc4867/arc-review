package dev.chieppa.arc.ui.switcher

import dev.chieppa.arc.core.recent.RecentSource
import dev.chieppa.arc.core.source.BranchName
import dev.chieppa.arc.core.source.RepoName
import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.core.source.SourceRefs
import dev.chieppa.arc.git.repo.LocalBranch
import dev.chieppa.arc.host.github.pulls.PullRequestSummary
import dev.chieppa.arc.host.github.pulls.Viewer
import dev.chieppa.arc.host.github.pulls.WaitingOn
import dev.chieppa.arc.host.github.pulls.WaitingOnRule
import dev.chieppa.arc.host.github.repo.GitHubRepo

/**
 * What followed repos hold that the strip does not, in the order they are suggested: the open [pulls] waiting on the
 * person ([viewer]), then what was opened lately ([recent]), then the other pulls by when they were updated, then the
 * local [branches] by when they were committed, then [commits], and last the branches with nothing ahead of the default
 * branch. Each pull lands in the local clone of its repo ([gitHubRepos] says which clone comes from which repo); a
 * recent source shows on its own only when it is in one of [repos] and nothing else here names it. Sources in the
 * strip, [inWorkspace], are left out.
 */
object FollowedResults {
    private const val WAITING = 0
    private const val RECENT = 1
    private const val PULL = 2
    private const val BRANCH = 3
    private const val COMMIT = 4
    private const val SPENT_BRANCH = 5

    fun of(
        pulls: List<PullRequestSummary>,
        branches: Map<RepoName, List<LocalBranch>>,
        gitHubRepos: Map<RepoName, GitHubRepo>,
        inWorkspace: Collection<Source>,
        recent: List<RecentSource> = emptyList(),
        viewer: Viewer? = null,
        repos: Collection<RepoName> = gitHubRepos.keys + branches.keys,
        commits: List<SwitcherItem.FollowedCommit> = emptyList(),
    ): List<SwitcherItem> {
        val held = inWorkspace.toSet()
        val pullItems = pulls.mapNotNull { pull ->
            val repo = gitHubRepos.entries.firstOrNull { it.value == pull.repo }?.key ?: return@mapNotNull null
            val source = Source.PullRequest(repo, pull.number)
            if (source in held) null else SwitcherItem.FollowedPull(source, pull)
        }.distinctBy { it.source }
        val branchItems = branches.flatMap { (repo, list) ->
            list.mapNotNull { branch ->
                val source = BranchName.parse(branch.name)?.let { Source.BranchAgainstBase(repo, it) } ?: return@mapNotNull null
                if (source in held) null else SwitcherItem.FollowedBranch(source, branch)
            }
        }
        val named = (pullItems.map { it.source } + branchItems.map { it.source }).toSet()
        val recentOrder = recent.mapIndexedNotNull { i, entry -> SourceRefs.parse(entry.ref)?.let { it to i } }.toMap()
        val recentItems = recent.mapNotNull { entry ->
            val source = SourceRefs.parse(entry.ref) ?: return@mapNotNull null
            if (source in held || source in named || source.repo !in repos) null else SwitcherItem.Recent(source, entry.title.orEmpty(), entry.at)
        }
        fun order(item: SwitcherItem): Triple<Int, Int, Long> {
            val source = sourceOf(item)
            val lately = recentOrder[source]
            return when {
                item is SwitcherItem.FollowedPull && WaitingOnRule.of(item.pull, viewer) is WaitingOn.You -> Triple(WAITING, 0, -item.pull.updatedAt.epochSecond)
                lately != null -> Triple(RECENT, lately, 0)
                item is SwitcherItem.FollowedPull -> Triple(PULL, 0, -item.pull.updatedAt.epochSecond)
                item is SwitcherItem.FollowedBranch && item.branch.ahead == 0 -> Triple(SPENT_BRANCH, 0, -item.branch.committed)
                item is SwitcherItem.FollowedBranch -> Triple(BRANCH, 0, -item.branch.committed)
                item is SwitcherItem.FollowedCommit -> Triple(COMMIT, 0, -item.commit.committed)
                else -> Triple(COMMIT + 1, 0, 0)
            }
        }
        val listed = named + recentItems.map { it.source }
        val commitItems = commits.filter { it.source !in held && it.source !in listed }.distinctBy { it.source }
        return (pullItems + recentItems + branchItems + commitItems).sortedWith(compareBy({ order(it).first }, { order(it).second }, { order(it).third }))
    }

    private fun sourceOf(item: SwitcherItem): Source? = when (item) {
        is SwitcherItem.FollowedPull -> item.source
        is SwitcherItem.FollowedBranch -> item.source
        is SwitcherItem.Recent -> item.source
        is SwitcherItem.FollowedCommit -> item.source
        else -> null
    }
}
