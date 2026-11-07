package dev.chieppa.arc.ui.review

import dev.chieppa.arc.core.mark.FileReviewState
import dev.chieppa.arc.core.mark.MarkKind
import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.ui.tree.ReviewTree
import dev.chieppa.arc.ui.tree.TreeOrder

/**
 * Marking files and walking them in the tree's order: the file being read is the focused pane's, when it shows a
 * change of the active source.
 */
class MarkController(private val state: WorkspaceState, private val panes: PaneController) {
    /** The active source and the path of the file being read in it, if any. */
    val current: Pair<Source, String>? get() {
        val pane = panes.focused ?: return null
        val source = pane.source?.takeIf { it == state.active && pane.hasChange } ?: return null
        return source to pane.path
    }

    /** Marks [paths] of [source] as reviewed, or clears their marks when [reviewed] is false. */
    fun setReviewed(source: Source, paths: Collection<String>, reviewed: Boolean) {
        mark(source, paths, if (reviewed) MarkKind.Reviewed else null)
    }

    /** Sets [paths] of [source] aside, out of the tree and the progress count. */
    fun setAside(source: Source, paths: Collection<String>) = mark(source, paths, MarkKind.SetAside)

    /** Marks [paths] reviewed unless all of them already are, in which case it clears them. */
    fun toggle(source: Source, paths: Collection<String>) {
        val states = state.states(source)
        setReviewed(source, paths, !paths.all { states[it] == FileReviewState.Reviewed })
    }

    /** Opens the next (or with [step] −1 the previous) file of the active source in the tree's order. */
    fun step(step: Int) {
        val source = state.active ?: return
        val files = TreeOrder.files(ReviewTree.roots(state), source)
        val target = TreeOrder.step(files, current?.second, step) ?: return
        panes.openChange(source, target.file.path)
    }

    /** Marks the file being read reviewed and opens the next one not reviewed yet. */
    fun markAndAdvance() {
        val (source, path) = current ?: return step(1)
        setReviewed(source, listOf(path), true)
        val files = TreeOrder.files(ReviewTree.roots(state), source)
        TreeOrder.nextToReview(files, path)?.let { panes.openChange(source, it.file.path) }
    }

    private fun mark(source: Source, paths: Collection<String>, kind: MarkKind?) {
        val snapshot = state.snapshot(source) ?: return
        state.workspace = Marking.mark(state.workspace, snapshot, paths, kind)
    }
}
