package dev.chieppa.arc.core.revision

import dev.chieppa.arc.core.conversation.ReviewThread
import dev.chieppa.arc.core.mark.FileMark
import dev.chieppa.arc.core.mark.FileReviewState
import dev.chieppa.arc.core.mark.MarkKind

/**
 * Builds a source's revision matrix. A file changed in a revision when its change against the base differs from the
 * one in the revision before, compared by fingerprint, so a rebase that only moves it changes nothing. A reviewed mark
 * covers the changes up to the revision it was made at, found by its reviewed head or else by its fingerprint; a
 * change after that counts as changed after review unless the mark still holds ([FileReviewState.Reviewed]), as the
 * tree decides.
 */
object RevisionMatrices {
    /**
     * The matrix of [history], with the files' [marks] and [states] by path, and the [threads] each counted in the
     * revision whose head it was made on.
     */
    fun of(
        history: RevisionHistory,
        marks: Map<String, FileMark> = emptyMap(),
        states: Map<String, FileReviewState> = emptyMap(),
        threads: List<ReviewThread> = emptyList(),
    ): RevisionMatrix {
        val revisions = history.revisions
        val paths = revisions.flatMap { it.fingerprints.keys }.distinct().sortedWith(TreePathOrder)
        val counts = threadCounts(revisions, threads)
        val rows = paths.map { path ->
            val mark = marks[path]?.takeIf { it.kind == MarkKind.Reviewed }
            val markedAt = mark?.let { markIndex(revisions, it, path) }
            val holds = states[path] == FileReviewState.Reviewed
            val cells = revisions.indices.map { i ->
                val changed = PatchChanges.differs(revisions.getOrNull(i - 1)?.fingerprints?.get(path), revisions[i].fingerprints[path])
                val kind = when {
                    !changed -> CellKind.Unchanged
                    mark == null -> CellKind.Changed
                    holds || (markedAt != null && i <= markedAt) -> CellKind.Reviewed
                    markedAt != null -> CellKind.ChangedAfterReview
                    else -> CellKind.Changed
                }
                MatrixCell(kind, counts[path to i] ?: ThreadCounts.NONE)
            }
            MatrixRow(path, cells, markedAt)
        }
        return RevisionMatrix(revisions, rows)
    }

    /** The revision [mark] was made at: the one with its reviewed head, else the last whose change of [path] it fingerprints. */
    private fun markIndex(revisions: List<SourceRevision>, mark: FileMark, path: String): Int? {
        val byHead = mark.reviewed?.let { reviewed -> revisions.indexOfFirst { it.head == reviewed.head } } ?: -1
        if (byHead >= 0) return byHead
        return revisions.indexOfLast { it.fingerprints[path]?.exact == mark.fingerprint.exact }.takeIf { it >= 0 }
    }

    private fun threadCounts(revisions: List<SourceRevision>, threads: List<ReviewThread>): Map<Pair<String, Int>, ThreadCounts> {
        val counts = mutableMapOf<Pair<String, Int>, ThreadCounts>()
        for (thread in threads) {
            val index = revisions.indexOfFirst { it.head == thread.madeOn }
            if (index < 0) continue
            val key = thread.path to index
            counts[key] = (counts[key] ?: ThreadCounts.NONE).plus(thread.state)
        }
        return counts
    }
}
