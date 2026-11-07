package dev.chieppa.arc.ui.revisions

import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.ui.workbench.Workbench

/** The branch [source] is compared against, when it names one: a pull request's base as its host said, or a branch's base. */
fun baseBranchOf(workbench: Workbench, source: Source): String? = when (source) {
    is Source.PullRequest -> workbench.state.pulls[source]?.baseBranch
    is Source.BranchAgainstBase -> source.base?.value
    is Source.Commit, is Source.CommitRange -> null
}
