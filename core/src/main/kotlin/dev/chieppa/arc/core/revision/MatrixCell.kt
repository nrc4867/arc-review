package dev.chieppa.arc.core.revision

/** One cell of the revision matrix: what happened to the file in the revision, and the threads made on it there. */
data class MatrixCell(val kind: CellKind, val threads: ThreadCounts = ThreadCounts.NONE)
