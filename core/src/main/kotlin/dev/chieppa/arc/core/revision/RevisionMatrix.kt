package dev.chieppa.arc.core.revision

/** A source's files against its revisions: [rows] in tree order, one cell per revision in [revisions]. */
data class RevisionMatrix(val revisions: List<SourceRevision>, val rows: List<MatrixRow>) {
    /** How many open threads the cells hold. */
    val openThreads: Int get() = rows.sumOf { row -> row.cells.sumOf { it.threads.open } }

    /** The row of [path], if the source ever changed it. */
    fun row(path: String): MatrixRow? = rows.firstOrNull { it.path == path }
}
