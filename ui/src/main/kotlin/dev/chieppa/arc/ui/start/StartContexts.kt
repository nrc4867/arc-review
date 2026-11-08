package dev.chieppa.arc.ui.start

import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.core.start.StartContext
import dev.chieppa.arc.core.start.StartScreenKind
import dev.chieppa.arc.core.start.StartSourceInfo
import dev.chieppa.arc.core.start.StartSourceKind
import dev.chieppa.arc.ui.review.WorkspaceState

/** The start screen's context as contributors are told it, from the window's state. */
object StartContexts {
    /** The screen for [state]'s selection, with what is known of each selected source. */
    fun of(state: WorkspaceState): StartContext {
        val selection = state.selection
        return StartContext(StartScreenKind.of(selection.size), selection.map { info(state, it) })
    }

    private fun info(state: WorkspaceState, source: Source): StartSourceInfo {
        val pull = state.pulls[source]
        val snapshot = state.snapshot(source)
        val progress = state.progress(source)
        val branch = source as? Source.BranchAgainstBase
        return StartSourceInfo(
            ref = source.ref,
            repo = source.repo.value,
            kind = kindOf(source),
            title = pull?.title ?: snapshot?.title,
            author = pull?.author,
            base = pull?.baseBranch ?: branch?.base?.value,
            head = pull?.headBranch ?: branch?.branch?.value,
            reviewed = progress?.reviewed,
            total = progress?.total,
        )
    }

    private fun kindOf(source: Source): StartSourceKind = when (source) {
        is Source.PullRequest -> StartSourceKind.PullRequest
        is Source.Commit -> StartSourceKind.Commit
        is Source.CommitRange -> StartSourceKind.CommitRange
        is Source.BranchAgainstBase -> StartSourceKind.Branch
    }
}
