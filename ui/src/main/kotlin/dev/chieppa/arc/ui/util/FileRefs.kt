package dev.chieppa.arc.ui.util

import dev.chieppa.arc.core.change.ChangeStatus
import dev.chieppa.arc.core.change.ChangedFile
import dev.chieppa.arc.core.change.SourceSnapshot
import dev.chieppa.arc.core.pane.Revision
import dev.chieppa.arc.core.source.RepoName

/** A file at a revision written GitHub-style, `repo@sha:path`, to paste into a comment or a terminal. */
object FileRefs {
    private const val AT = "@"
    private const val COLON = ":"

    fun of(repo: RepoName, revision: Revision, path: String): String = repo.value + AT + revision.value + COLON + path

    /** [file] in [snapshot] at its head, or at its base for a deleted file, where it still is. */
    fun of(snapshot: SourceSnapshot, file: ChangedFile): String {
        val revisions = snapshot.revisions
        val at = if (file.status == ChangeStatus.Deleted) revisions.base else revisions.head
        return of(snapshot.source.repo, Revision(at.value), file.path)
    }
}
