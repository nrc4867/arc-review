package dev.chieppa.arc.ui.switcher

import dev.chieppa.arc.core.source.BranchName
import dev.chieppa.arc.core.source.RepoName
import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.git.repo.LocalBranch
import dev.chieppa.arc.host.github.pulls.PullRequestSummary
import dev.chieppa.arc.host.github.repo.GitHubRepo
import dev.chieppa.stretch.frame.matchScore

/**
 * What followed repos hold that the review does not: the open [pulls] GitHub found, each in the local clone of its
 * repo ([gitHubRepos] says which clone comes from which repo), and the local [branches] whose names match [query].
 * Sources the review holds, [inReview], are left out.
 */
object FollowedResults {
    fun of(
        query: String,
        pulls: List<PullRequestSummary>,
        branches: Map<RepoName, List<LocalBranch>>,
        gitHubRepos: Map<RepoName, GitHubRepo>,
        inReview: Collection<Source>,
    ): List<SwitcherItem> {
        if (query.isBlank()) return emptyList()
        val held = inReview.toSet()
        val heldBranches = held.filterIsInstance<Source.BranchAgainstBase>().map { it.repo to it.branch }.toSet()
        val pullItems = pulls.mapNotNull { pull ->
            val repo = gitHubRepos.entries.firstOrNull { it.value == pull.repo }?.key ?: return@mapNotNull null
            val source = Source.PullRequest(repo, pull.number)
            if (source in held) null else SwitcherItem.FollowedPull(source, pull)
        }.distinctBy { it.source }
        val branchItems = branches.flatMap { (repo, list) ->
            list.mapNotNull { branch ->
                val name = BranchName.parse(branch.name) ?: return@mapNotNull null
                if (repo to name in heldBranches || matchScore(query, branch.name) == null) {
                    null
                } else {
                    SwitcherItem.FollowedBranch(Source.BranchAgainstBase(repo, name), branch)
                }
            }
        }
        return pullItems + branchItems
    }
}
