package dev.chieppa.arc.ui.menus

import androidx.compose.runtime.Composable
import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.ui.actions.ArcActionIds
import dev.chieppa.arc.ui.workbench.MenuTarget
import dev.chieppa.arc.ui.workbench.Workbench

/** The context menu of a source, on its chip or its tree row: make it active, remove it, copy its ref, open its revision matrix. */
@Composable
fun SourceMenu(workbench: Workbench, source: Source, close: () -> Unit, target: MenuTarget = MenuTarget.OfSource(source)) {
    TargetActionItem(workbench, ArcActionIds.ACTIVATE_SOURCE, target, close)
    TargetActionItem(workbench, ArcActionIds.REMOVE_SOURCE, target, close)
    TargetActionItem(workbench, ArcActionIds.COPY_SOURCE_REF, target, close)
    TargetActionItem(workbench, ArcActionIds.OPEN_REVISIONS, target, close)
}
