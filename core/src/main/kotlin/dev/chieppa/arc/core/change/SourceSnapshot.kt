package dev.chieppa.arc.core.change

import dev.chieppa.arc.core.mark.PatchFingerprint
import dev.chieppa.arc.core.source.Source

/**
 * A source as read from its repo: the [revisions] it compares, its [commits] oldest first, the changed [files] in
 * path order, and each file's [fingerprints] by path. [title] is the head commit's subject.
 */
data class SourceSnapshot(
    val source: Source,
    val revisions: SourceRevisions,
    val title: String,
    val commits: List<CommitInfo>,
    val files: List<ChangedFile>,
    val fingerprints: Map<String, PatchFingerprint> = emptyMap(),
) {
    /** The changed file at [path], if there is one. */
    fun file(path: String): ChangedFile? = files.firstOrNull { it.path == path }
}
