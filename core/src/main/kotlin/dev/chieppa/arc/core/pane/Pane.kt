package dev.chieppa.arc.core.pane

import dev.chieppa.arc.core.source.RepoName
import dev.chieppa.arc.core.source.Source

/**
 * One view of one file at one revision. A pane belongs to the review, not to a source, so a file from one repo can
 * sit beside a file from another. With a [base] it can show the file's change from [base] to [revision]; [oldPath] is
 * where the file was at [base] when it moved. [source] is the source it was opened from, if any. A [pinned] pane stays
 * when the active source changes.
 */
data class Pane(
    val id: PaneId,
    val repo: RepoName,
    val path: String,
    val revision: Revision,
    val view: PaneView = PaneView.Diff,
    val pinned: Boolean = false,
    val base: Revision? = null,
    val oldPath: String? = null,
    val source: Source? = null,
) {
    init {
        require(path.isNotBlank() && !path.startsWith('/') && !path.endsWith('/')) { "Not a file path in a repo: '$path'" }
    }

    /** Whether the pane has a change to show. */
    val hasChange: Boolean get() = base != null

    /** The last part of [path], the name a tab shows. */
    val fileName: String get() = path.substringAfterLast('/')
}
