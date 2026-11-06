package dev.chieppa.arc.ui.actions

import dev.chieppa.arc.core.pane.PaneView
import dev.chieppa.arc.ui.workbench.ReviewTargets
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.core.Action
import dev.chieppa.stretch.core.ActionId
import dev.chieppa.stretch.core.ActionRegistry
import dev.chieppa.stretch.core.KeyChord

/** Registers the actions for reading a review on [workbench]: moving through files and changes, marking, panes. */
fun registerReviewActions(registry: ActionRegistry, workbench: Workbench) {
    fun action(id: ActionId, key: KeyChord? = null, available: () -> Boolean = { true }, run: () -> Unit) =
        registry.register(Action(id, id.value, id.value, listOfNotNull(key), isAvailable = available, run = run))

    val state = workbench.state
    val hasSource = { state.active != null }
    val hasPane = { workbench.panes.focused != null }

    action(ArcActionIds.ADD_SOURCE) { workbench.addSourceOpen = true }
    action(ArcActionIds.OPEN_FILE, ArcKeys.OPEN_FILE, hasSource) { workbench.finderOpen = true }
    action(ArcActionIds.NEXT_FILE, ArcKeys.NEXT_FILE, hasSource) { workbench.marks.step(1) }
    action(ArcActionIds.PREVIOUS_FILE, ArcKeys.PREVIOUS_FILE, hasSource) { workbench.marks.step(-1) }
    action(ArcActionIds.NEXT_CHANGE, ArcKeys.NEXT_CHANGE, hasPane) { workbench.onFocusedPane { nextChange() } }
    action(ArcActionIds.PREVIOUS_CHANGE, ArcKeys.PREVIOUS_CHANGE, hasPane) { workbench.onFocusedPane { previousChange() } }
    action(ArcActionIds.FIND_IN_FILE, ArcKeys.FIND_IN_FILE, hasPane) { workbench.onFocusedPane { openFind() } }
    action(ArcActionIds.MARK_AND_ADVANCE, ArcKeys.MARK_AND_ADVANCE, hasSource) {
        val targets = ReviewTargets.of(workbench)
        if (workbench.treeFocused && targets.isNotEmpty()) {
            targets.forEach { (source, paths) -> workbench.marks.setReviewed(source, paths, true) }
        } else {
            workbench.marks.markAndAdvance()
        }
    }
    action(ArcActionIds.TOGGLE_REVIEWED, ArcKeys.TOGGLE_REVIEWED, hasSource) {
        ReviewTargets.of(workbench).forEach { (source, paths) -> workbench.marks.toggle(source, paths) }
    }
    action(ArcActionIds.SET_ASIDE, available = hasSource) {
        ReviewTargets.of(workbench).forEach { (source, paths) -> workbench.marks.setAside(source, paths) }
    }
    action(ArcActionIds.TOGGLE_PIN, available = hasPane) { workbench.panes.focused?.let { workbench.panes.togglePin(it.id.value) } }
    action(ArcActionIds.CLOSE_PANE, ArcKeys.CLOSE_PANE, hasPane) { workbench.panes.focused?.let { workbench.panes.close(it.id.value) } }
    action(ArcActionIds.TOGGLE_FULL_FILE, available = { workbench.panes.focused?.hasChange == true }) {
        val pane = workbench.panes.focused ?: return@action
        workbench.panes.setView(pane.id.value, if (pane.view == PaneView.Diff) PaneView.FullFile else PaneView.Diff)
    }
    action(ArcActionIds.TOGGLE_SPLIT, available = { workbench.panes.focused?.hasChange == true }) {
        val pane = workbench.panes.focused ?: return@action
        workbench.panes.setSplit(pane.id.value, !state.paneState(pane.id.value).split)
    }
}
