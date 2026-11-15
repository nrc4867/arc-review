package dev.chieppa.arc.ui.navigation

import dev.chieppa.arc.codeintel.model.SymbolTarget
import dev.chieppa.arc.core.pane.Revision
import dev.chieppa.arc.core.source.RepoName
import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.ui.review.PaneController
import dev.chieppa.arc.ui.review.WorkspaceState

/**
 * Opens the file a navigation target or a place in the history is in: as its source's change when the change touches
 * it at that revision, else whole at the revision, reusing a pane already showing it. Returns where the pane should
 * scroll.
 */
class SymbolOpener(private val state: WorkspaceState, private val panes: PaneController) {
    fun open(target: SymbolTarget): NavReveal {
        val document = target.location.document
        val repo = RepoName(document.repo)
        val source = state.workspace.sources.firstOrNull { it.ref == target.source }
        if (!openChange(source, document.revision, document.path)) openWhole(repo, document.revision, document.path, source)
        val range = target.location.range
        val end = range.end.column.takeIf { range.end.line == range.start.line && it > range.start.column }
        return NavReveal(repo, document.revision, document.path, range.start.line, range.start.column, end = end)
    }

    /** Opens the file [place] is in, as its change when it was shown as one, and returns where its pane should scroll. */
    fun open(place: NavPlace): NavReveal {
        val source = place.source?.takeIf { it in state.workspace.sources }
        val base = place.base
        when {
            base == null -> openWhole(place.repo, place.revision, place.path, source)
            !openChange(source, place.revision, place.path) -> panes.openDiff(place.repo, place.path, Revision(base), Revision(place.revision), source)
        }
        return place.reveal()
    }

    /** Opens [path] of [source]'s change when the source is read at [revision] and changes it; whether it did. */
    private fun openChange(source: Source?, revision: String, path: String): Boolean {
        val snapshot = source?.let(state::snapshot) ?: return false
        return snapshot.revisions.head.value == revision && snapshot.file(path) != null && panes.openChange(source, path)
    }

    /** Shows the pane showing [path] whole at [revision], or opens one. */
    private fun openWhole(repo: RepoName, revision: String, path: String, source: Source?) {
        val shown = state.workspace.panes.firstOrNull {
            it.repo == repo && it.path == path && it.revision.value == revision && it.base == null && state.paneLayout.leafOf(it.id.value) != null
        }
        if (shown != null) panes.show(shown.id.value) else panes.openFile(repo, Revision(revision), path, source)
    }
}
