package dev.chieppa.arc.ui.actions

import dev.chieppa.arc.core.mark.FileReviewState
import dev.chieppa.arc.core.pane.PaneView
import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.ui.tree.ReviewTree
import dev.chieppa.arc.ui.tree.TreeFolds
import dev.chieppa.arc.ui.util.FileRefs
import dev.chieppa.arc.ui.workbench.ActionTargets
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.core.Action
import dev.chieppa.stretch.core.ActionId
import dev.chieppa.stretch.core.ActionRegistry
import dev.chieppa.stretch.frame.PaneItemDrop

/**
 * Registers the actions the context menus offer, on sources, tree rows and tabs. Each acts on the open menu's target,
 * or from the palette on the tree's selection, the focused pane and the active source (see [ActionTargets]).
 */
fun registerContextActions(registry: ActionRegistry, workbench: Workbench) {
    fun action(id: ActionId, available: () -> Boolean, run: () -> Unit) =
        registry.register(Action(id, id.value, id.value, emptyList(), isAvailable = available, run = run))

    val state = workbench.state
    val hasFile = { ActionTargets.file(workbench) != null }
    fun withFile(run: (Source, String) -> Unit) = { ActionTargets.file(workbench)?.let { (source, path) -> run(source, path) } ?: Unit }
    fun states(within: Map<Source, List<String>>) =
        within.flatMap { (source, paths) -> state.states(source).let { s -> paths.map { s[it] } } }

    action(ArcActionIds.ACTIVATE_SOURCE, { ActionTargets.source(workbench)?.let { it != state.active } == true }) {
        ActionTargets.source(workbench)?.let(workbench::activateSource)
    }
    action(ArcActionIds.REMOVE_SOURCE, { ActionTargets.source(workbench) != null }) {
        ActionTargets.source(workbench)?.let(workbench::removeSource)
    }
    action(ArcActionIds.COPY_SOURCE_REF, { ActionTargets.source(workbench) != null }) {
        ActionTargets.source(workbench)?.let { workbench.clipboard.copy(it.ref) }
    }

    action(ArcActionIds.OPEN_CHANGE, hasFile, withFile { source, path -> workbench.panes.openChange(source, path) })
    action(ArcActionIds.OPEN_IN_NEW_TAB, hasFile, withFile { source, path -> workbench.panes.openChange(source, path, newTab = true) })
    action(ArcActionIds.OPEN_TO_SIDE, hasFile, withFile { source, path -> workbench.panes.openChangeToSide(source, path) })
    action(ArcActionIds.OPEN_IN_NEW_WINDOW, { hasFile() && workbench.newWindowBounds != null }, withFile { source, path ->
        workbench.newWindowBounds?.let { workbench.panes.openChangeAt(source, path, PaneItemDrop.InWindow(it)) }
    })
    action(ArcActionIds.OPEN_PINNED, hasFile, withFile { source, path ->
        val id = workbench.panes.changePane(source, path, newTab = true) ?: return@withFile
        if (state.paneLayout.leafOf(id) == null) state.paneLayout = state.paneLayout.open(id)
        if (state.pane(id)?.pinned == false) workbench.panes.togglePin(id)
    })
    action(ArcActionIds.OPEN_FULL_FILE, hasFile, withFile { source, path ->
        if (workbench.panes.openChange(source, path)) workbench.panes.focused?.let { workbench.panes.setView(it.id.value, PaneView.FullFile) }
    })

    action(ArcActionIds.RESTORE, { states(ActionTargets.files(workbench)).any { it == FileReviewState.SetAside } }) {
        ActionTargets.files(workbench).forEach { (source, paths) -> workbench.marks.setReviewed(source, paths, false) }
    }
    action(ArcActionIds.COPY_PATH, { ActionTargets.folder(workbench) != null || hasFile() || ActionTargets.pane(workbench) != null }) {
        val path = ActionTargets.folder(workbench)?.path ?: ActionTargets.file(workbench)?.second ?: ActionTargets.pane(workbench)?.path
        path?.let(workbench.clipboard::copy)
    }
    action(ArcActionIds.COPY_FILE_REF, { hasFile() || ActionTargets.pane(workbench) != null }) {
        val ref = ActionTargets.file(workbench)?.let { (source, path) ->
            val snapshot = state.snapshot(source)
            snapshot?.file(path)?.let { FileRefs.of(snapshot, it) }
        } ?: ActionTargets.pane(workbench)?.let { FileRefs.of(it.repo, it.revision, it.path) }
        ref?.let(workbench.clipboard::copy)
    }

    action(ArcActionIds.EXPAND_ALL, { state.review.sources.isNotEmpty() }) {
        workbench.treeState.expanded += TreeFolds.parents(ReviewTree.roots(state, workbench.sinceReview), ActionTargets.folder(workbench)?.key)
    }
    action(ArcActionIds.COLLAPSE_ALL, { state.review.sources.isNotEmpty() }) {
        workbench.treeState.expanded -= TreeFolds.parents(ReviewTree.roots(state, workbench.sinceReview), ActionTargets.folder(workbench)?.key)
    }
}
