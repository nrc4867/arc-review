package dev.chieppa.arc.ui.pulls

import androidx.compose.runtime.Composable
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.pull_copy
import dev.chieppa.arc.design.generated.pull_open_review
import dev.chieppa.arc.ui.actions.ArcActionIds
import dev.chieppa.arc.ui.menus.TargetActionItem
import dev.chieppa.arc.ui.workbench.MenuTarget
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.frame.SubMenuItem
import org.jetbrains.compose.resources.stringResource

/**
 * The context menu of a pull request's row: open it in this review, add it beside the active source, open it in a new
 * review or in the saved review that holds it, open it on GitHub, and a Copy submenu of its link, number, reference,
 * branch, head commit and title.
 */
@Composable
fun PullMenu(workbench: Workbench, target: MenuTarget.OfPull, close: () -> Unit) {
    TargetActionItem(workbench, ArcActionIds.OPEN_PULL, target, close)
    TargetActionItem(workbench, ArcActionIds.ADD_PULL, target, close)
    TargetActionItem(workbench, ArcActionIds.OPEN_PULL_IN_NEW_REVIEW, target, close)
    val holding = target.source?.let(workbench::otherReviewHolding)
    if (holding != null) TargetActionItem(workbench, ArcActionIds.OPEN_PULL_REVIEW, target, close, stringResource(Res.string.pull_open_review, holding.name))
    TargetActionItem(workbench, ArcActionIds.OPEN_PULL_ON_GITHUB, target, close)
    SubMenuItem(stringResource(Res.string.pull_copy)) {
        TargetActionItem(workbench, ArcActionIds.COPY_PULL_LINK, target, close)
        TargetActionItem(workbench, ArcActionIds.COPY_PULL_NUMBER, target, close)
        TargetActionItem(workbench, ArcActionIds.COPY_PULL_REFERENCE, target, close)
        TargetActionItem(workbench, ArcActionIds.COPY_PULL_BRANCH, target, close)
        TargetActionItem(workbench, ArcActionIds.COPY_PULL_HEAD, target, close)
        TargetActionItem(workbench, ArcActionIds.COPY_PULL_TITLE, target, close)
    }
}
