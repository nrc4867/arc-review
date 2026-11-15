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
 * Arc's items in a tab's context menu, after Stretch's own: pin or unpin, mark its file, copy its path or ref, open the
 * file or its source on GitHub. A page's tab offers its source on GitHub when it has one.
 */
@Composable
fun TabMenu(workbench: Workbench, id: String, close: () -> Unit) {
    val target = MenuTarget.OfPane(id)
    val pane = workbench.state.pane(id)
    if (pane == null) {
        TargetActionItem(workbench, ArcActionIds.OPEN_ON_GITHUB, target, close)
        return
    }
    TargetActionItem(workbench, ArcActionIds.TOGGLE_PIN, target, close, stringResource(if (pane.pinned) Res.string.menu_unpin else Res.string.menu_pin))
    if (pane.hasChange && pane.source != null) MarkItems(workbench, target, close)
    MenuDivider()
    TargetActionItem(workbench, ArcActionIds.COPY_PATH, target, close)
    TargetActionItem(workbench, ArcActionIds.COPY_FILE_REF, target, close)
    MenuDivider()
    TargetActionItem(workbench, ArcActionIds.OPEN_FILE_ON_GITHUB, target, close)
    TargetActionItem(workbench, ArcActionIds.OPEN_ON_GITHUB, target, close)
}
