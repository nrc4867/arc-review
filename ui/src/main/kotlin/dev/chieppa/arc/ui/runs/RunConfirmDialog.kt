package dev.chieppa.arc.ui.runs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import dev.chieppa.arc.core.actions.RunState
import dev.chieppa.arc.design.ArcIcons
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.comment_cancel
import dev.chieppa.arc.design.generated.confirm_approve_fork
import dev.chieppa.arc.design.generated.confirm_approve_fork_title
import dev.chieppa.arc.design.generated.confirm_cancel
import dev.chieppa.arc.design.generated.confirm_cancel_rerun
import dev.chieppa.arc.design.generated.confirm_cancel_title
import dev.chieppa.arc.design.generated.confirm_debug
import dev.chieppa.arc.design.generated.confirm_fork_changes
import dev.chieppa.arc.design.generated.confirm_fork_no_changes
import dev.chieppa.arc.design.generated.confirm_rerun
import dev.chieppa.arc.design.generated.confirm_rerun_failed
import dev.chieppa.arc.design.generated.confirm_rerun_job
import dev.chieppa.arc.design.generated.confirm_rerun_title
import dev.chieppa.arc.design.generated.confirm_run
import dev.chieppa.arc.design.generated.confirm_sent_as_you
import dev.chieppa.stretch.frame.StretchDialog
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchSpacing
import dev.chieppa.stretch.ui.Button
import dev.chieppa.stretch.ui.ButtonKind
import dev.chieppa.stretch.ui.Checkbox
import dev.chieppa.stretch.ui.Spinner
import org.jetbrains.compose.resources.stringResource

/**
 * Asks before a write to a run goes to GitHub: which run and what will happen, debug logging for a re-run, and for a
 * fork's run every change its pull request makes under `.github/`, the code that runs with the repo's permissions. A
 * refused write stays, its line in the error color.
 */
@Composable
fun RunConfirmDialog(controller: RunController) {
    val confirm = controller.confirming ?: return
    val c = Stretch.colors
    val run = confirm.run
    val (title, action) = when (confirm) {
        is RunConfirm.Rerun -> Res.string.confirm_rerun_title to if (confirm.failedOnly) Res.string.confirm_rerun_failed else Res.string.confirm_rerun
        is RunConfirm.RerunJob -> Res.string.confirm_rerun_title to Res.string.confirm_rerun_job
        is RunConfirm.Cancel -> Res.string.confirm_cancel_title to if (confirm.job != null) Res.string.confirm_cancel_rerun else Res.string.confirm_cancel
        is RunConfirm.ApproveFork -> Res.string.confirm_approve_fork_title to Res.string.confirm_approve_fork
    }
    val icon = when (confirm) {
        is RunConfirm.Cancel -> ArcIcons.Stop
        is RunConfirm.ApproveFork -> ArcIcons.Gate
        else -> ArcIcons.Refresh
    }
    val reading = confirm is RunConfirm.ApproveFork && confirm.reading
    StretchDialog(
        stringResource(title),
        controller::dismiss,
        icon = icon,
        width = RunSizes.confirmDialog,
        actions = {
            RunText(stringResource(Res.string.confirm_sent_as_you), RunTextKind.Small, if (controller.failed) c.errorInk else c.inkSubtle, Modifier.weight(1f))
            Button(stringResource(Res.string.comment_cancel), controller::dismiss, kind = ButtonKind.Quiet)
            Button(stringResource(action), controller::confirm, kind = ButtonKind.Primary, icon = icon, enabled = !controller.busy && !reading)
        },
    ) {
        Column(Modifier.fillMaxWidth().padding(StretchSpacing.space12), verticalArrangement = Arrangement.spacedBy(StretchSpacing.space10)) {
            RunText(stringResource(Res.string.confirm_run, run.workflowName, run.number, run.repo, runRefText(run)), RunTextKind.Body, if (controller.failed) c.errorInk else c.ink, maxLines = 2)
            val job = (confirm as? RunConfirm.RerunJob)?.job ?: (confirm as? RunConfirm.Cancel)?.job
            job?.let { RunText(it.name, RunTextKind.Strong, c.inkStrong) }
            if (confirm is RunConfirm.Rerun || confirm is RunConfirm.RerunJob) {
                Checkbox(controller.debug, { controller.debug = it }, stringResource(Res.string.confirm_debug))
            }
            if (confirm is RunConfirm.ApproveFork) ForkChangesList(confirm)
        }
    }
}

@Composable
private fun ForkChangesList(confirm: RunConfirm.ApproveFork) {
    val c = Stretch.colors
    if (confirm.reading) return Spinner(stringResource(Res.string.confirm_fork_changes, 0))
    val changes = confirm.changes?.changes.orEmpty()
    if (changes.isEmpty()) return RunText(stringResource(Res.string.confirm_fork_no_changes), RunTextKind.Small, c.inkSubtle)
    RunText(stringResource(Res.string.confirm_fork_changes, changes.size), RunTextKind.Eyebrow, c.warnInk)
    val shape = Stretch.shapes.ctlShape
    Column(
        Modifier.fillMaxWidth().heightIn(max = RunSizes.previewHeight).background(c.warnWash, shape).border(Stretch.stroke, c.warnEdge, shape)
            .verticalScroll(rememberScrollState()).padding(StretchSpacing.space10),
        verticalArrangement = Arrangement.spacedBy(StretchSpacing.space8),
    ) {
        for (change in changes) {
            RunText("${change.status} ${change.path}", RunTextKind.MonoSmall, c.inkStrong)
            change.patch?.let { BasicText(it, style = Stretch.type.valueSm.merge(TextStyle(color = c.ink))) }
        }
    }
}
