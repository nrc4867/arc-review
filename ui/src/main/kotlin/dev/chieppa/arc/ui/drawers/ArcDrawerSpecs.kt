package dev.chieppa.arc.ui.drawers

import androidx.compose.runtime.Composable
import dev.chieppa.arc.ui.conversation.StagedActions
import dev.chieppa.arc.ui.conversation.StagedCommentsContent
import dev.chieppa.arc.ui.files.FilesDrawerContent
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.frame.DrawerSpec
import org.jetbrains.compose.resources.stringResource

/** The frame's drawers, one per [DrawerSlot]: Files and Staged comments hold the review; the others open plain. */
@Composable
fun arcDrawerSpecs(workbench: Workbench): List<DrawerSpec> = ArcDrawerSlots.all.map { slot ->
    when (slot.id) {
        DrawerIds.FILES -> DrawerSpec(slot.id, stringResource(slot.title), slot.icon) { FilesDrawerContent(workbench) }
        DrawerIds.STAGED_COMMENTS -> DrawerSpec(slot.id, stringResource(slot.title), slot.icon, actions = { StagedActions(workbench) }) {
            StagedCommentsContent(workbench)
        }
        else -> DrawerSpec(slot.id, stringResource(slot.title), slot.icon) {}
    }
}
