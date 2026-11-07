package dev.chieppa.arc.ui.drawers

import androidx.compose.runtime.Composable
import dev.chieppa.arc.ui.conversation.StagedActions
import dev.chieppa.arc.ui.conversation.StagedCommentsContent
import dev.chieppa.arc.ui.files.FilesDrawerContent
import dev.chieppa.arc.design.ArcIcons
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.drawer_commits
import dev.chieppa.arc.ui.pulls.PullRequestsDrawer
import dev.chieppa.arc.ui.revisions.CommitsDrawer
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.frame.DrawerSpec
import org.jetbrains.compose.resources.stringResource

/**
 * The frame's drawers, one per [DrawerSlot]: Files and Staged comments hold the review, Pull requests the saved filters
 * and notification rules, Revisions the active source's commits under the title Commits; the others open plain.
 */
@Composable
fun arcDrawerSpecs(workbench: Workbench): List<DrawerSpec> = ArcDrawerSlots.all.map { slot ->
    when (slot.id) {
        DrawerIds.FILES -> DrawerSpec(slot.id, stringResource(slot.title), slot.icon, scrolls = false) { FilesDrawerContent(workbench) }
        DrawerIds.PULL_REQUESTS -> DrawerSpec(slot.id, stringResource(slot.title), slot.icon) { PullRequestsDrawer(workbench) }
        DrawerIds.REVISIONS -> DrawerSpec(
            slot.id,
            stringResource(Res.string.drawer_commits),
            slot.icon,
            headerIcon = ArcIcons.Commit,
            railLabel = stringResource(slot.title),
        ) { CommitsDrawer(workbench) }
        DrawerIds.STAGED_COMMENTS -> DrawerSpec(slot.id, stringResource(slot.title), slot.icon, actions = { StagedActions(workbench) }) {
            StagedCommentsContent(workbench)
        }
        else -> DrawerSpec(slot.id, stringResource(slot.title), slot.icon) {}
    }
}
