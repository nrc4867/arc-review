package dev.chieppa.arc.ui.tree

import dev.chieppa.arc.core.mark.FileReviewState
import dev.chieppa.arc.ui.review.WorkspaceState
import dev.chieppa.arc.ui.review.SourceLoad
import dev.chieppa.stretch.ui.TreeNode

/**
 * The file tree for the workspace: every source in the strip, each with the files its view shows; with
 * [onlyChangedSince], only the files whose change differs from what was reviewed.
 */
object ReviewTree {
    fun roots(state: WorkspaceState, onlyChangedSince: Boolean = false): List<TreeNode<FileTreeItem>> = FileTreeBuilder.build(
        state.workspace.sources.map { source ->
            val whole = state.loads[source]
            val shown = state.shown(source)
            val states = state.states(source)
            SourceRows(
                source = source,
                title = whole?.ready?.title.orEmpty(),
                files = shown?.ready?.files?.filter { !onlyChangedSince || states[it.path] == FileReviewState.ChangedSinceReviewed },
                states = states,
                progress = state.progress(source),
                failed = whole is SourceLoad.Failed || shown is SourceLoad.Failed,
                ended = state.pulls[source]?.state?.ended == true,
            )
        },
    )
}
