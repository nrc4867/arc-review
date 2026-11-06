package dev.chieppa.arc.ui.review

import dev.chieppa.arc.core.pane.Pane
import dev.chieppa.arc.core.pane.PaneId
import dev.chieppa.arc.core.pane.PaneView
import dev.chieppa.arc.core.pane.Revision
import dev.chieppa.arc.core.source.RepoName
import dev.chieppa.arc.core.source.Source

/**
 * The review's panes: opening a changed file or any file at a revision, closing, pinning, and switching what a pane
 * shows. Opening a changed file reuses the pane showing it, or takes over the focused pane's unpinned diff, so walking
 * the files keeps one pane; [newTab] opens it beside instead.
 */
class PaneController(private val state: ReviewState) {
    /** The pane showing in the focused part of the grid. */
    val focused: Pane? get() = state.paneLayout.activeTab?.let(state::pane)

    /** Opens [path] of [source]'s change; false when the source is not read yet or does not change [path]. */
    fun openChange(source: Source, path: String, newTab: Boolean = false): Boolean {
        val load = state.shown(source) ?: return false
        val snapshot = load.ready ?: return false
        val file = snapshot.file(path) ?: return false
        if (source != state.active) {
            val (review, views) = SourceSwitching.activate(state.review, state.views, source)
            state.review = review
            state.views = views
        }
        val existing = state.review.panes.firstOrNull { it.source == source && it.path == path && it.hasChange && it.revision.value == snapshot.revisions.head.value }
        if (existing != null && state.paneLayout.leafOf(existing.id.value) != null) {
            state.paneLayout = state.paneLayout.activate(existing.id.value)
            return true
        }
        val reuse = focused?.takeIf { !newTab && !it.pinned && it.hasChange && it.source == source }
        val id = reuse?.id ?: PaneFactory.nextId(state.review)
        val pane = PaneFactory.forChange(id, snapshot, file).copy(view = reuse?.view?.takeIf { it != PaneView.Rendered } ?: PaneView.Diff)
        state.review = state.review.withPane(pane)
        if (reuse != null) state.paneLines.remove(id.value)
        state.paneLayout = state.paneLayout.open(id.value)
        return true
    }

    /** Opens [path] in [repo] at [revision] whole, in a new tab; [pinned] keeps it when the source changes. */
    fun openFile(repo: RepoName, revision: Revision, path: String, source: Source? = null, pinned: Boolean = false): PaneId {
        val id = PaneFactory.nextId(state.review)
        state.review = state.review.withPane(PaneFactory.forFile(id, repo, revision, path, source).copy(pinned = pinned))
        state.paneLayout = state.paneLayout.open(id.value)
        if (pinned) placePinned()
        return id
    }

    /** Closes the pane [id] everywhere it shows; a pane no layout shows any more leaves the review. */
    fun close(id: String) {
        state.paneLayout = state.paneLayout.close(id)
        val pane = state.pane(id) ?: return
        if (pane.pinned) state.views = SourceSwitching.withoutEverywhere(state.views, id, null)
        val shown = state.views.values.any { it.layout.leafOf(id) != null } || state.looseLayout.leafOf(id) != null
        if (!shown) {
            state.review = state.review.withoutPane(pane.id)
            state.paneStates = state.paneStates - id
        }
    }

    /** Pins the pane [id] so it stays when the source changes, or unpins it so it stays with this source only. */
    fun togglePin(id: String) {
        val pane = state.pane(id) ?: return
        state.review = state.review.withPane(pane.copy(pinned = !pane.pinned))
        if (pane.pinned) state.views = SourceSwitching.withoutEverywhere(state.views, id, state.active) else placePinned()
    }

    fun setView(id: String, view: PaneView) {
        val pane = state.pane(id) ?: return
        state.review = state.review.withPane(pane.copy(view = view))
    }

    fun setSplit(id: String, split: Boolean) {
        state.setPaneState(id, state.paneState(id).copy(split = split))
    }

    private fun placePinned() {
        val pinned = state.review.panes.filter { it.pinned }.map { it.id.value }
        state.views = state.views.mapValues { (source, view) ->
            if (source == state.active) view else view.copy(layout = SourceSwitching.withPinned(view.layout, pinned))
        }
    }
}
