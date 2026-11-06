package dev.chieppa.arc.core.mark

import dev.chieppa.arc.core.source.RepoName

/** A mark on one file's change, held against the repo, the path and the change's [patch], not against a commit. */
data class FileMark(
    val repo: RepoName,
    val path: String,
    val patch: PatchHash,
    val kind: MarkKind,
)
