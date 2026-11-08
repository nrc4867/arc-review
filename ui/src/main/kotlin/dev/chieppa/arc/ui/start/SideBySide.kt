package dev.chieppa.arc.ui.start

import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.frame.PaneEdge

/**
 * Opens the next file to review of each of several sources in panes side by side, in strip order. The panes are
 * pinned, so they stay side by side whichever of the sources is active; the source active before stays active.
 */
object SideBySide {
    /** The sources of [sources] that have a file left to review. */
    fun openable(workbench: Workbench, sources: List<Source>): List<Source> =
        sources.filter { StartReview.nextFile(workbench.state, it) != null }

    fun open(workbench: Workbench, sources: List<Source>) {
        val state = workbench.state
        val active = state.active ?: return
        val ids = openable(workbench, sources).mapNotNull { source ->
            val path = StartReview.nextFile(state, source) ?: return@mapNotNull null
            workbench.panes.changePane(source, path, newTab = true)?.also { id ->
                state.pane(id)?.let { pane -> state.workspace = state.workspace.withPane(pane.copy(pinned = true)) }
            }
        }
        if (ids.isEmpty()) return
        workbench.activateSource(active)
        var layout = ids.fold(state.paneLayout) { layout, id -> layout.close(id) }
        layout = layout.open(ids.first())
        for ((previous, id) in ids.zipWithNext()) {
            val leaf = layout.leafOf(previous)?.id ?: continue
            layout = layout.open(id, leaf).splitWith(id, leaf, PaneEdge.End)
        }
        state.paneLayout = layout
    }
}
