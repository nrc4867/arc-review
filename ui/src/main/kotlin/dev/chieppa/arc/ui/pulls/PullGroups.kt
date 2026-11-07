package dev.chieppa.arc.ui.pulls

import dev.chieppa.arc.core.watch.FilterGrouping
import dev.chieppa.arc.host.github.pulls.PullRequestSummary

/** Grouping a filter's pull requests as its tab shows them, each group most recently updated first. */
object PullGroups {
    fun of(pulls: List<PullRequestSummary>, grouping: FilterGrouping): List<PullGroup> {
        val recent = pulls.sortedByDescending { it.updatedAt }
        return when (grouping) {
            FilterGrouping.None -> if (recent.isEmpty()) emptyList() else listOf(PullGroup(null, null, recent))
            FilterGrouping.Repo -> recent.groupBy { it.repo.fullName }.map { (name, group) -> PullGroup(name, null, group) }
                .sortedWith(compareByDescending<PullGroup> { it.pulls.size }.thenBy { it.name })
            FilterGrouping.Label -> byLabel(recent)
        }
    }

    /** Each pull request under its first label, the biggest groups first, those without a label last. */
    private fun byLabel(pulls: List<PullRequestSummary>): List<PullGroup> {
        val (labelled, bare) = pulls.partition { it.labels.isNotEmpty() }
        val groups = labelled.groupBy { it.labels.first().name }
            .map { (name, group) -> PullGroup(name, group.first().labels.first(), group) }
            .sortedWith(compareByDescending<PullGroup> { it.pulls.size }.thenBy { it.name })
        return if (bare.isEmpty()) groups else groups + PullGroup(null, null, bare)
    }
}
