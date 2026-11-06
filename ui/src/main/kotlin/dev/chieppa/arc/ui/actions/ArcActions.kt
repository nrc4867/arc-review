package dev.chieppa.arc.ui.actions

import dev.chieppa.arc.ui.drawers.ArcDrawerSlots
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.core.Action
import dev.chieppa.stretch.core.ActionId
import dev.chieppa.stretch.core.ActionRegistry
import dev.chieppa.stretch.core.KeyChord

/** Registers every Arc action that runs on [workbench] with [registry]. [onQuit] closes the app. */
fun registerArcActions(registry: ActionRegistry, workbench: Workbench, onQuit: () -> Unit) {
    fun action(id: ActionId, keys: List<KeyChord> = emptyList(), run: () -> Unit) =
        registry.register(Action(id, id.value, id.value, keys, run = run))

    action(ArcActionIds.GO_TO_ANYTHING, listOf(ArcKeys.GO_TO_ANYTHING)) { workbench.paletteOpen = true }
    action(ArcActionIds.SETTINGS) { workbench.settingsOpen = true }
    action(ArcActionIds.RESET_LAYOUT) { workbench.resetLayout() }
    action(ArcActionIds.QUIT, run = onQuit)
    for (slot in ArcDrawerSlots.all) {
        action(ArcActionIds.toggleDrawer(slot.id)) { workbench.toggleDrawer(slot.id) }
    }
}
