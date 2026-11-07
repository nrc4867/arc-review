package dev.chieppa.arc.ui.review

import dev.chieppa.arc.core.change.SourceSnapshot
import dev.chieppa.arc.core.mark.FileMark
import dev.chieppa.arc.core.mark.MarkKind
import dev.chieppa.arc.core.mark.ReviewedRevisions
import dev.chieppa.arc.core.workspace.Workspace

/** Marking files of a source's whole change, as reviewed, set aside, or not at all. */
object Marking {
    /** [workspace] with [paths] of [snapshot] marked [kind], or unmarked when [kind] is null. Paths not in it are skipped. */
    fun mark(workspace: Workspace, snapshot: SourceSnapshot, paths: Collection<String>, kind: MarkKind?): Workspace {
        val source = snapshot.source
        if (kind == null) return workspace.unmark(source, paths)
        val reviewed = ReviewedRevisions(snapshot.revisions.head, snapshot.revisions.base)
        val marks = paths.mapNotNull { path ->
            snapshot.fingerprints[path]?.let { FileMark(source, path, it, kind, reviewed) }
        }
        return workspace.mark(marks)
    }
}
