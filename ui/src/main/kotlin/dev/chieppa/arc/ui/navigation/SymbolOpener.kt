package dev.chieppa.arc.ui.navigation

import dev.chieppa.arc.codeintel.model.SymbolTarget
import dev.chieppa.arc.core.pane.Revision
import dev.chieppa.arc.core.source.RepoName
import dev.chieppa.arc.ui.review.PaneController
import dev.chieppa.arc.ui.review.WorkspaceState

/**
 * Opens the file a navigation target is in: as its source's change when the change touches it at that revision,
 * else whole at the target's revision, reusing a pane already showing it. Returns where the pane should scroll.
 */
class SymbolOpener(private val state: WorkspaceState, private val panes: PaneController) {
    fun open(target: SymbolTarget): NavReveal {
        val document = target.location.document
        val repo = RepoName(document.repo)
        val source = state.workspace.sources.firstOrNull { it.ref == target.source }
        val snapshot = source?.let(state::snapshot)
        val changed = snapshot != null && snapshot.revisions.head.value == document.revision && snapshot.file(document.path) != null
        if (source != null && changed) {
            panes.openChange(source, document.path)
        } else {
            val shown = state.workspace.panes.firstOrNull {
                it.repo == repo && it.path == document.path && it.revision.value == document.revision && it.base == null &&
                    state.paneLayout.leafOf(it.id.value) != null
            }
            if (shown != null) panes.show(shown.id.value) else panes.openFile(repo, Revision(document.revision), document.path, source)
        }
        val at = target.location.range.start
        return NavReveal(repo, document.revision, document.path, at.line, at.column)
    }
}
