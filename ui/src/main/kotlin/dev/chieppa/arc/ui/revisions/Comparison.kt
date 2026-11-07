package dev.chieppa.arc.ui.revisions

import dev.chieppa.arc.core.change.ObjectId
import dev.chieppa.arc.core.change.SourceRevisions
import dev.chieppa.arc.core.revision.SourceRevision

/**
 * Two commits of a source picked for comparing, older first: each as its change from the base of the revision that
 * brought it ([from], [to]), and those revisions.
 */
data class Comparison(val from: SourceRevisions, val to: SourceRevisions, val fromRevision: SourceRevision, val toRevision: SourceRevision) {
    /** The two commits, older first. */
    val key: Pair<ObjectId, ObjectId> get() = from.head to to.head

    /** Whether the two changes start from different bases, so their patches are compared through a rebase. */
    val throughRebase: Boolean get() = from.base != to.base
}
