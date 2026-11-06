package dev.chieppa.arc.ui.menus

import androidx.compose.runtime.Composable
import dev.chieppa.arc.core.mark.FileReviewState
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.menu_mark_all_reviewed
import dev.chieppa.arc.design.generated.menu_mark_reviewed
import dev.chieppa.arc.design.generated.menu_set_aside_folder
import dev.chieppa.arc.design.generated.menu_unmark_all
import dev.chieppa.arc.design.generated.menu_unmark_reviewed
import dev.chieppa.arc.ui.actions.ArcActionIds
import dev.chieppa.arc.ui.workbench.ActionTargets
import dev.chieppa.arc.ui.workbench.MenuTarget
import dev.chieppa.arc.ui.workbench.Workbench
import org.jetbrains.compose.resources.stringResource

/**
 * The marking items of a context menu on [target]: mark or unmark reviewed, named for what it will do, and set aside
 * or restore. [many] names them for a folder's files.
 */
@Composable
fun MarkItems(workbench: Workbench, target: MenuTarget, close: () -> Unit, many: Boolean = false) {
    val files = workbench.withTarget(target) { ActionTargets.files(workbench) }
    if (files.values.all { it.isEmpty() }) return
    val allReviewed = files.all { (source, paths) -> workbench.state.states(source).let { s -> paths.all { s[it] == FileReviewState.Reviewed } } }
    val label = when {
        many && allReviewed -> Res.string.menu_unmark_all
        many -> Res.string.menu_mark_all_reviewed
        allReviewed -> Res.string.menu_unmark_reviewed
        else -> Res.string.menu_mark_reviewed
    }
    TargetActionItem(workbench, ArcActionIds.TOGGLE_REVIEWED, target, close, stringResource(label))
    TargetActionItem(workbench, ArcActionIds.SET_ASIDE, target, close, if (many) stringResource(Res.string.menu_set_aside_folder) else null)
    TargetActionItem(workbench, ArcActionIds.RESTORE, target, close)
}
