package dev.chieppa.arc.ui.runs

import androidx.compose.runtime.Composable
import dev.chieppa.arc.core.actions.WorkflowRun
import dev.chieppa.arc.ui.actions.ArcActionIds
import dev.chieppa.arc.ui.menus.TargetActionItem
import dev.chieppa.arc.ui.workbench.MenuTarget
import dev.chieppa.arc.ui.workbench.Workbench

/**
 * The context menu of a run's row or page: open it, open its commit as a source, watch it or its workflow, re-run,
 * cancel, approve a fork's run, start its workflow again, open it on GitHub and copy its link.
 */
@Composable
fun RunMenu(workbench: Workbench, run: WorkflowRun, close: () -> Unit) {
    val target = MenuTarget.OfRun(run)
    for (id in RunActionList.ROW) TargetActionItem(workbench, id, target, close)
}
