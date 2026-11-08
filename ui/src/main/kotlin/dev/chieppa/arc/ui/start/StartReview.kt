package dev.chieppa.arc.ui.start

import dev.chieppa.arc.core.mark.FileReviewState
import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.ui.review.WorkspaceState
import dev.chieppa.arc.ui.tree.ReviewTree
import dev.chieppa.arc.ui.tree.TreeOrder

/** Where a source's review stands, as the start screen tells it. */
object StartReview {
    /** The path of [source]'s first file not reviewed yet, in the tree's order, or null when every file is. */
    fun nextFile(state: WorkspaceState, source: Source): String? =
        TreeOrder.nextToReview(TreeOrder.files(ReviewTree.roots(state), source), null)?.file?.path

    /** How many of [source]'s files stand at [file] state. */
    fun count(state: WorkspaceState, source: Source, file: FileReviewState): Int = state.states(source).values.count { it == file }

    /** How [source]'s files stand, or null before it is read. */
    fun counts(state: WorkspaceState, source: Source): ReviewCounts? {
        if (state.snapshot(source) == null) return null
        val states = state.states(source).values
        fun count(file: FileReviewState) = states.count { it == file }
        return ReviewCounts(count(FileReviewState.Reviewed), count(FileReviewState.ChangedSinceReviewed), count(FileReviewState.SetAside), count(FileReviewState.NotReviewed))
    }

    /** The file name at the end of [path]. */
    fun name(path: String): String = path.substringAfterLast('/')
}
