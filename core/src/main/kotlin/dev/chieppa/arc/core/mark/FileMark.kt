package dev.chieppa.arc.core.mark

import dev.chieppa.arc.core.source.RepoName
import dev.chieppa.arc.core.source.Source

/**
 * A mark on one file's change in one [source], held against the change's [fingerprint] and the [reviewed] revisions,
 * not against a commit, so a rebase that keeps the change keeps the mark.
 */
data class FileMark(
    val source: Source,
    val path: String,
    val fingerprint: PatchFingerprint,
    val kind: MarkKind,
    val reviewed: ReviewedRevisions? = null,
) {
    val repo: RepoName get() = source.repo
}
