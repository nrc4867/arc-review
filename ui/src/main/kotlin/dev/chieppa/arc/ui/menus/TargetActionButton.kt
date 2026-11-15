package dev.chieppa.arc.ui.menus

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import dev.chieppa.arc.ui.actions.actionIcon
import dev.chieppa.arc.ui.util.TipIconButton
import dev.chieppa.arc.ui.workbench.MenuTarget
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.core.ActionId
import dev.chieppa.stretch.frame.LocalActionText
import dev.chieppa.stretch.frame.caps
import dev.chieppa.stretch.theme.StretchSizes

/**
 * An icon button for the registered action [id], acting on [target]: its icon, and its label and key in the tooltip,
 * from the registry and keymap. Nothing shows while the action is not available for the target.
 */
@Composable
fun TargetActionButton(workbench: Workbench, id: ActionId, target: MenuTarget, size: Dp = StretchSizes.seg) {
    val action = workbench.registry[id] ?: return
    if (!workbench.withTarget(target) { action.isAvailable() }) return
    val icon = action.icon?.let(::actionIcon) ?: return
    TipIconButton(
        icon,
        LocalActionText.current(action.textKey),
        { workbench.withTarget(target) { action.run() } },
        size = size,
        keys = workbench.keymap.keysFor(id).firstOrNull()?.caps(),
    )
}
