package dev.chieppa.arc.core.pane

import dev.chieppa.arc.core.source.RepoName

/**
 * One view of one file at one revision. A pane belongs to the review, not to a source, so a file from one repo can
 * sit beside a file from another. A [pinned] pane stays when the active source changes.
 */
data class Pane(
    val id: PaneId,
    val repo: RepoName,
    val path: String,
    val revision: Revision,
    val view: PaneView = PaneView.Diff,
    val pinned: Boolean = false,
) {
    init {
        require(path.isNotBlank() && !path.startsWith('/') && !path.endsWith('/')) { "Not a file path in a repo: '$path'" }
    }

    /** The last part of [path], the name a tab shows. */
    val fileName: String get() = path.substringAfterLast('/')
}
