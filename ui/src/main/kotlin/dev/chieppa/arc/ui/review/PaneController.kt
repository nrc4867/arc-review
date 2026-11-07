package dev.chieppa.arc.ui.review

import dev.chieppa.arc.core.pane.Page
import dev.chieppa.arc.core.pane.Pane
import dev.chieppa.arc.core.pane.PaneId
import dev.chieppa.arc.core.pane.PaneView
import dev.chieppa.arc.core.pane.Revision
import dev.chieppa.arc.core.source.RepoName
import dev.chieppa.arc.core.source.Source
import dev.chieppa.stretch.frame.PaneDrop
import dev.chieppa.stretch.frame.PaneEdge
import dev.chieppa.stretch.frame.PaneItemDrop
import dev.chieppa.stretch.frame.place

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
        val id = changePane(source, path, newTab) ?: return false
        val layout = state.paneLayout
        state.paneLayout = if (layout.leafOf(id) != null) layout.activate(id) else layout.open(id)
        return true
    }

    /**
     * The id of the pane for [path] of [source]'s change, with [source] made active: the pane already showing it, the
     * focused unpinned diff taken over unless [newTab], or a new pane not yet in the layout. Null when the source is
     * not read yet or does not change [path].
     */
    fun changePane(source: Source, path: String, newTab: Boolean = false): String? {
        val load = state.shown(source) ?: return null
        val snapshot = load.ready ?: return null
        val file = snapshot.file(path) ?: return null
        if (source != state.active) {
            val (review, views) = SourceSwitching.activate(state.review, state.views, source)
            state.review = review
            state.views = views
        }
        val existing = state.review.panes.firstOrNull { it.source == source && it.path == path && it.hasChange && it.revision.value == snapshot.revisions.head.value }
        if (existing != null && state.paneLayout.leafOf(existing.id.value) != null) return existing.id.value
        val reuse = focused?.takeIf { !newTab && !it.pinned && it.hasChange && it.source == source }
        val id = reuse?.id ?: PaneFactory.nextId(state.review)
        val pane = PaneFactory.forChange(id, snapshot, file).copy(view = reuse?.view?.takeIf { it != PaneView.Rendered } ?: PaneView.Diff)
        state.review = state.review.withPane(pane)
        if (reuse != null) state.paneLines.remove(id.value)
        return id.value
    }

    /** Opens [path] of [source]'s change in its own pane wherever [drop] says: a pane, an edge or a window. */
    fun openChangeAt(source: Source, path: String, drop: PaneItemDrop): Boolean {
        val id = changePane(source, path, newTab = true) ?: return false
        state.paneLayout = state.paneLayout.place(id, drop)
        return true
    }

    /** Opens [path] of [source]'s change in a new pane split off the focused one at its end. */
    fun openChangeToSide(source: Source, path: String): Boolean =
        openChangeAt(source, path, PaneItemDrop.InPane(PaneDrop.Edge(state.paneLayout.focusedLeaf.id, PaneEdge.End)))

    /** Opens [path] in [repo] at [revision] whole, in a new tab; [pinned] keeps it when the source changes. */
    fun openFile(repo: RepoName, revision: Revision, path: String, source: Source? = null, pinned: Boolean = false): PaneId {
        val id = PaneFactory.nextId(state.review)
        state.review = state.review.withPane(PaneFactory.forFile(id, repo, revision, path, source).copy(pinned = pinned))
        state.paneLayout = state.paneLayout.open(id.value)
        if (pinned) placePinned()
        return id
    }

    /**
     * Opens [path] in [repo]'s change from [base] to [revision] in a new tab, or returns the pane already showing that
     * change in the layout. [source] is the source it belongs to.
     */
    fun openDiff(repo: RepoName, path: String, base: Revision, revision: Revision, source: Source?): PaneId {
        val existing = state.review.panes.firstOrNull { it.repo == repo && it.path == path && it.base == base && it.revision == revision }
        if (existing != null && state.paneLayout.leafOf(existing.id.value) != null) return existing.id
        val id = PaneFactory.nextId(state.review)
        state.review = state.review.withPane(Pane(id, repo, path, revision, PaneView.Diff, base = base, source = source))
        state.paneLayout = state.paneLayout.open(id.value)
        return id
    }

    /** Shows the tab [id] in its part of the grid. */
    fun show(id: String) {
        if (state.paneLayout.leafOf(id) != null) state.paneLayout = state.paneLayout.activate(id)
    }

    /** Opens the revision matrix of [source], or shows its tab when one is open already. */
    fun openRevisions(source: Source) {
        val existing = state.review.pages.firstOrNull { it is Page.RevisionMatrix && it.source == source }
        if (existing != null && state.paneLayout.leafOf(existing.id.value) != null) {
            state.paneLayout = state.paneLayout.activate(existing.id.value)
            return
        }
        val page = existing ?: Page.RevisionMatrix(PaneFactory.nextId(state.review), source)
        state.review = state.review.withPage(page)
        state.paneLayout = state.paneLayout.open(page.id.value)
    }

    /** Opens the page for saved filter [filter], or shows its tab when one is open already. */
    fun openFilter(filter: String) {
        val existing = state.review.pages.firstOrNull { it is Page.Filter && it.filter == filter }
        if (existing != null && state.paneLayout.leafOf(existing.id.value) != null) {
            state.paneLayout = state.paneLayout.activate(existing.id.value)
            return
        }
        val page = existing ?: Page.Filter(PaneFactory.nextId(state.review), filter)
        state.review = state.review.withPage(page)
        state.paneLayout = state.paneLayout.open(page.id.value)
        placePinned()
    }

    /** Closes the pane or page [id] everywhere it shows; one no layout shows any more leaves the review. */
    fun close(id: String) {
        state.paneLayout = state.paneLayout.close(id)
        state.page(id)?.let { page ->
            if (page.pinned) state.views = SourceSwitching.withoutEverywhere(state.views, id, null)
            state.looseLayout = state.looseLayout.close(id)
            state.review = state.review.withoutPage(page.id)
            return
        }
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

    fun setWrap(id: String, wrap: Boolean) {
        state.setPaneState(id, state.paneState(id).copy(wrap = wrap))
    }

    private fun placePinned() {
        val pinned = state.review.pinnedIds
        state.views = state.views.mapValues { (source, view) ->
            if (source == state.active) view else view.copy(layout = SourceSwitching.withPinned(view.layout, pinned))
        }
    }
}
