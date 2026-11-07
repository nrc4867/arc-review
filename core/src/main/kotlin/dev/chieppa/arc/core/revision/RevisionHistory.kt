package dev.chieppa.arc.core.revision

import dev.chieppa.arc.core.change.CommitInfo
import dev.chieppa.arc.core.change.ObjectId
import dev.chieppa.arc.core.source.Source

/** The heads [source] has had that can be read, as [revisions] oldest first. */
data class RevisionHistory(val source: Source, val revisions: List<SourceRevision>) {
    /** Every commit the revisions brought, oldest first, each with the revision that brought it. */
    val commits: List<Pair<CommitInfo, SourceRevision>> get() = revisions.flatMap { revision -> revision.commits.map { it to revision } }

    /** The revision that brought [commit], or whose head it is. */
    fun revisionOf(commit: ObjectId): SourceRevision? =
        revisions.firstOrNull { it.head == commit } ?: revisions.firstOrNull { revision -> revision.commits.any { it.id == commit } }

    /** How many revisions rewrote the one before. */
    val rewrites: Int get() = revisions.count { it.rewrote }
}
