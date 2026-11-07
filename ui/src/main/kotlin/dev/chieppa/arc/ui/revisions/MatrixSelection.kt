package dev.chieppa.arc.ui.revisions

import dev.chieppa.arc.core.revision.CellKind
import dev.chieppa.arc.core.revision.MatrixRow

/** The selected cell of the revision matrix: the file at [path] in the revision at [index]. */
data class MatrixSelection(val path: String, val index: Int) {
    /**
     * The selection moved [rows] down (up when negative) and [columns] across among [shown] rows, held at the edges;
     * the selection itself when its row is not shown.
     */
    fun moved(shown: List<MatrixRow>, rows: Int, columns: Int): MatrixSelection {
        val at = shown.indexOfFirst { it.path == path }
        if (at < 0) return this
        val row = shown[(at + rows).coerceIn(0, shown.lastIndex)]
        return MatrixSelection(row.path, (index + columns).coerceIn(0, row.cells.lastIndex))
    }

    companion object {
        /** Where a matrix of [shown] rows starts: the first row's last changed cell, or null for no rows. */
        fun first(shown: List<MatrixRow>): MatrixSelection? {
            val row = shown.firstOrNull() ?: return null
            val last = row.cells.indexOfLast { it.kind != CellKind.Unchanged }
            return MatrixSelection(row.path, if (last >= 0) last else row.cells.lastIndex.coerceAtLeast(0))
        }
    }
}
