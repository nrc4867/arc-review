package dev.chieppa.arc.ui.menus

import androidx.compose.runtime.Composable
import dev.chieppa.arc.ui.actions.ArcActionIds
import dev.chieppa.arc.ui.tree.FileTreeItem
import dev.chieppa.arc.ui.workbench.MenuTarget
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.frame.MenuDivider

/**
 * The context menu of a file tree row. A file opens in each way a pane can take it, is marked and copied; a folder
 * marks, sets aside, opens and closes everything under it; a source is made active, removed or copied.
 */
@Composable
fun TreeRowMenu(workbench: Workbench, target: MenuTarget.OfRows, close: () -> Unit) {
    when (val item = target.item) {
        is FileTreeItem.FileRow -> {
            for (id in OPEN_ITEMS) TargetActionItem(workbench, id, target, close)
            MenuDivider()
            MarkItems(workbench, target, close)
            MenuDivider()
            TargetActionItem(workbench, ArcActionIds.COPY_PATH, target, close)
            TargetActionItem(workbench, ArcActionIds.COPY_FILE_REF, target, close)
        }
        is FileTreeItem.FolderRow, is FileTreeItem.AsideRow -> {
            MarkItems(workbench, target, close, many = true)
            MenuDivider()
            TargetActionItem(workbench, ArcActionIds.EXPAND_ALL, target, close)
            TargetActionItem(workbench, ArcActionIds.COLLAPSE_ALL, target, close)
            if (item is FileTreeItem.FolderRow) {
                MenuDivider()
                TargetActionItem(workbench, ArcActionIds.COPY_PATH, target, close)
            }
        }
        is FileTreeItem.SourceRow -> SourceMenu(workbench, item.source, close, target)
    }
}

private val OPEN_ITEMS = listOf(
    ArcActionIds.OPEN_CHANGE,
    ArcActionIds.OPEN_IN_NEW_TAB,
    ArcActionIds.OPEN_TO_SIDE,
    ArcActionIds.OPEN_IN_NEW_WINDOW,
    ArcActionIds.OPEN_PINNED,
    ArcActionIds.OPEN_FULL_FILE,
)
