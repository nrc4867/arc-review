package dev.chieppa.arc.core.revision

/**
 * One file's row in the revision matrix: a cell per revision, by index, and the index of the revision its reviewed
 * mark was made at, if it has one.
 */
data class MatrixRow(val path: String, val cells: List<MatrixCell>, val markedAt: Int? = null) {
    /** Whether the file changed after the revision it was reviewed at. */
    val changedAfterReview: Boolean get() = cells.any { it.kind == CellKind.ChangedAfterReview }

    /** Whether any thread was made on the file in any revision. */
    val hasThreads: Boolean get() = cells.any { !it.threads.isEmpty }

    /** The last revision before [index] that changed the file. */
    fun lastChangeBefore(index: Int): Int? = (index - 1 downTo 0).firstOrNull { cells[it].kind.changed }

    /**
     * The revision to compare the file at [index] with: the one it was reviewed at when that came before, else the
     * last one before that changed it; null when nothing before changed it, so its change starts from the base.
     */
    fun diffFrom(index: Int): Int? = markedAt?.takeIf { it < index } ?: lastChangeBefore(index)
}
