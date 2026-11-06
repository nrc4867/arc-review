package dev.chieppa.arc.ui.menus

import androidx.compose.runtime.Composable
import dev.chieppa.arc.ui.actions.actionIcon
import dev.chieppa.arc.ui.workbench.MenuTarget
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.core.ActionId
import dev.chieppa.stretch.frame.LocalActionText
import dev.chieppa.stretch.frame.MenuItem
import dev.chieppa.stretch.frame.caps

/**
 * A context menu item for the registered action [id], acting on [target]: its icon and key from the registry and
 * keymap, its label from the action or [text]. An action not available for the target is left out.
 */
@Composable
fun TargetActionItem(workbench: Workbench, id: ActionId, target: MenuTarget, close: () -> Unit, text: String? = null) {
    val action = workbench.registry[id] ?: return
    if (!workbench.withTarget(target) { action.isAvailable() }) return
    MenuItem(
        text = text ?: LocalActionText.current(action.textKey),
        onClick = {
            workbench.withTarget(target) { action.run() }
            close()
        },
        icon = action.icon?.let(::actionIcon),
        keys = workbench.keymap.keysFor(id).firstOrNull()?.caps()?.joinToString(" "),
    )
}
