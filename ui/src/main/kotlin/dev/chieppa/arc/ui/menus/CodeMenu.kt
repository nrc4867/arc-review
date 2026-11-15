package dev.chieppa.arc.ui.menus

import androidx.compose.runtime.Composable
import dev.chieppa.arc.ui.actions.ArcActionIds
import dev.chieppa.arc.ui.workbench.MenuTarget
import dev.chieppa.arc.ui.workbench.Workbench

/** The context menu over a pane's code, acting on the name a right-click put the caret on: go to it, to what implements it, or list its usages. */
@Composable
fun CodeMenu(workbench: Workbench, paneId: String, close: () -> Unit) {
    val target = MenuTarget.OfPane(paneId)
    TargetActionItem(workbench, ArcActionIds.GO_TO_DEFINITION, target, close)
    TargetActionItem(workbench, ArcActionIds.GO_TO_IMPLEMENTATION, target, close)
    TargetActionItem(workbench, ArcActionIds.SHOW_USAGES, target, close)
}
