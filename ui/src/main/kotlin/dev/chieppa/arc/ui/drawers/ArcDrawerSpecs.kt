package dev.chieppa.arc.ui.drawers

import androidx.compose.runtime.Composable
import dev.chieppa.arc.ui.activity.ConsoleDrawer
import dev.chieppa.arc.ui.activity.ConsoleDrawerActions
import dev.chieppa.arc.ui.conversation.ConversationDrawer
import dev.chieppa.arc.ui.conversation.NotesDrawer
import dev.chieppa.arc.ui.conversation.NotesExportButton
import dev.chieppa.arc.ui.conversation.StagedCommentsContent
import dev.chieppa.arc.ui.conversation.stagedComments
import dev.chieppa.arc.ui.files.FilesDrawerContent
import dev.chieppa.arc.design.ArcIcons
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.drawer_commits
import dev.chieppa.arc.ui.navigation.OutlineDrawer
import dev.chieppa.arc.ui.pulls.PullRequestsDrawer
import dev.chieppa.arc.ui.revisions.CommitsDrawer
import dev.chieppa.arc.ui.runs.RunsDrawer
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.frame.DrawerSpec
import org.jetbrains.compose.resources.stringResource

/**
 * The frame's drawers, one per [DrawerSlot]: Files holds the workspace; Outline the focused pane's symbols; Notes, Conversation and Staged comments the
 * active source's notes, pull request conversation and staged batch; Pull requests the saved filters and notification
 * rules; Runs the run filters and run notifications; Revisions the active source's commits under the title Commits;
 * Console the activity log; the others open plain.
 */
@Composable
fun arcDrawerSpecs(workbench: Workbench): List<DrawerSpec> = ArcDrawerSlots.all.map { slot ->
    when (slot.id) {
        DrawerIds.FILES -> DrawerSpec(slot.id, stringResource(slot.title), slot.icon, scrolls = false) { FilesDrawerContent(workbench) }
        DrawerIds.OUTLINE -> DrawerSpec(slot.id, stringResource(slot.title), slot.icon, scrolls = false) { OutlineDrawer(workbench) }
        DrawerIds.PULL_REQUESTS -> DrawerSpec(slot.id, stringResource(slot.title), slot.icon) { PullRequestsDrawer(workbench) }
        DrawerIds.RUNS -> DrawerSpec(slot.id, stringResource(slot.title), slot.icon) { RunsDrawer(workbench) }
        DrawerIds.REVISIONS -> DrawerSpec(
            slot.id,
            stringResource(Res.string.drawer_commits),
            slot.icon,
            headerIcon = ArcIcons.Commit,
            railLabel = stringResource(slot.title),
        ) { CommitsDrawer(workbench) }
        DrawerIds.STAGED_COMMENTS -> DrawerSpec(
            slot.id,
            stringResource(slot.title),
            slot.icon,
            badge = stagedComments(workbench).size.takeIf { it > 0 }?.toString(),
            scrolls = false,
        ) { StagedCommentsContent(workbench) }
        DrawerIds.CONVERSATION -> DrawerSpec(slot.id, stringResource(slot.title), slot.icon, scrolls = false) { ConversationDrawer(workbench) }
        DrawerIds.NOTES -> DrawerSpec(slot.id, stringResource(slot.title), slot.icon, scrolls = false, actions = { NotesExportButton(workbench) }) {
            NotesDrawer(workbench)
        }
        DrawerIds.CONSOLE -> DrawerSpec(slot.id, stringResource(slot.title), slot.icon, scrolls = false, actions = { ConsoleDrawerActions(workbench.console) }) {
            ConsoleDrawer(workbench.console)
        }
        else -> DrawerSpec(slot.id, stringResource(slot.title), slot.icon) {}
    }
}
