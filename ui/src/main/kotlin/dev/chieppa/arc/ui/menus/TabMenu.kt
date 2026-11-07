package dev.chieppa.arc.ui.menus

import androidx.compose.runtime.Composable
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.menu_pin
import dev.chieppa.arc.design.generated.menu_unpin
import dev.chieppa.arc.ui.actions.ArcActionIds
import dev.chieppa.arc.ui.workbench.MenuTarget
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.frame.MenuDivider
import org.jetbrains.compose.resources.stringResource

/**
 * Arc's items in a tab's context menu, after Stretch's own: pin or unpin, mark its file, copy its path or ref. A page's
 * tab has Stretch's own only.
 */
@Composable
fun TabMenu(workbench: Workbench, id: String, close: () -> Unit) {
    val pane = workbench.state.pane(id) ?: return
    val target = MenuTarget.OfPane(id)
    TargetActionItem(workbench, ArcActionIds.TOGGLE_PIN, target, close, stringResource(if (pane.pinned) Res.string.menu_unpin else Res.string.menu_pin))
    if (pane.hasChange && pane.source != null) MarkItems(workbench, target, close)
    MenuDivider()
    TargetActionItem(workbench, ArcActionIds.COPY_PATH, target, close)
    TargetActionItem(workbench, ArcActionIds.COPY_FILE_REF, target, close)
}
