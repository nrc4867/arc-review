package dev.chieppa.arc.ui.runs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.chieppa.arc.design.ArcIcons
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.comment_cancel
import dev.chieppa.arc.design.generated.dispatch_branch
import dev.chieppa.arc.design.generated.dispatch_goes_as
import dev.chieppa.arc.design.generated.dispatch_inputs
import dev.chieppa.arc.design.generated.dispatch_line
import dev.chieppa.arc.design.generated.dispatch_read_from
import dev.chieppa.arc.design.generated.dispatch_repo
import dev.chieppa.arc.design.generated.dispatch_runs_as
import dev.chieppa.arc.design.generated.dispatch_workflow
import dev.chieppa.arc.design.generated.run_workflow
import dev.chieppa.arc.ui.util.ArcSizes
import dev.chieppa.stretch.frame.Dropdown
import dev.chieppa.stretch.frame.StretchDialog
import dev.chieppa.stretch.theme.Icon
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchSpacing
import dev.chieppa.stretch.ui.Button
import dev.chieppa.stretch.ui.ButtonKind
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

/**
 * The Run workflow form: repo, workflow and branch, the inputs its manual trigger takes read from the workflow file at
 * the branch's head, and what goes to GitHub, which the person confirms with Run workflow. A workflow with no manual
 * trigger, or a required input left empty, is marked in place and the button stays off; a refused dispatch marks the
 * line it would have sent.
 */
@Composable
fun RunWorkflowDialog(controller: RunController) {
    val form = controller.dispatching ?: return
    val c = Stretch.colors
    val scope = rememberCoroutineScope()
    var refused by remember(form) { mutableStateOf(false) }
    val close = { controller.dispatching = null }
    StretchDialog(
        stringResource(Res.string.run_workflow),
        close,
        icon = ArcIcons.RunWorkflow,
        width = RunSizes.dialog,
        actions = {
            RunText(stringResource(Res.string.dispatch_runs_as), RunTextKind.Small, c.inkSubtle, Modifier.weight(1f))
            Button(stringResource(Res.string.comment_cancel), close, kind = ButtonKind.Quiet)
            Button(stringResource(Res.string.run_workflow), { scope.launch { refused = !controller.dispatch(form) } }, kind = ButtonKind.Primary, icon = ArcIcons.RunWorkflow, enabled = form.ready)
        },
    ) {
        Column(Modifier.fillMaxWidth().padding(StretchSpacing.space12), verticalArrangement = Arrangement.spacedBy(StretchSpacing.space10)) {
            FormRow(stringResource(Res.string.dispatch_repo)) {
                Dropdown(form.repos.sortedBy { it.fullName.lowercase() }, form.repo, { if (it != null) form.pickRepo(it) }, { it?.fullName.orEmpty() }, stringResource(Res.string.dispatch_repo), Modifier.fillMaxWidth(), icon = ArcIcons.Repo)
            }
            FormRow(stringResource(Res.string.dispatch_workflow)) {
                Dropdown(form.workflows, form.workflow, { if (it != null) form.pickWorkflow(it) }, { it?.let { w -> "${w.name} · ${w.fileName}" }.orEmpty() }, stringResource(Res.string.dispatch_workflow), Modifier.fillMaxWidth(), icon = ArcIcons.Runs)
            }
            FormRow(stringResource(Res.string.dispatch_branch)) {
                Dropdown(form.branches, form.branch, { if (it != null) form.pickBranch(it) }, { it?.let { b -> "${b.name} · ${b.sha.take(SHORT_SHA)}" }.orEmpty() }, stringResource(Res.string.dispatch_branch), Modifier.fillMaxWidth(), icon = ArcIcons.Branch)
            }
            val workflow = form.workflow
            val branch = form.branch
            if (workflow != null && branch != null) {
                val noTrigger = !form.reading && form.spec?.dispatchable != true
                Row(horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space6), verticalAlignment = Alignment.CenterVertically) {
                    Icon(ArcIcons.Runs, if (noTrigger) c.warnInk else c.inkSubtle, size = ArcSizes.smallIcon)
                    RunText(stringResource(Res.string.dispatch_read_from, workflow.path, branch.sha.take(SHORT_SHA)), RunTextKind.Small, if (noTrigger) c.warnInk else c.inkSubtle)
                }
            }
        }
        if (form.inputs.isNotEmpty()) {
            Column(Modifier.fillMaxWidth().padding(horizontal = StretchSpacing.space12), verticalArrangement = Arrangement.spacedBy(StretchSpacing.space10)) {
                RunText(stringResource(Res.string.dispatch_inputs), RunTextKind.Eyebrow, c.inkMuted)
                for (input in form.inputs) InputRow(input, form)
            }
        }
        if (workflowReady(form)) {
            val shape = Stretch.shapes.ctlShape
            Column(Modifier.fillMaxWidth().padding(StretchSpacing.space12), verticalArrangement = Arrangement.spacedBy(StretchSpacing.space6)) {
                RunText(stringResource(Res.string.dispatch_goes_as), RunTextKind.Eyebrow, c.inkMuted)
                Column(
                    Modifier.fillMaxWidth().background(if (refused) c.errorWash else c.surfaceCanvas, shape).border(Stretch.stroke, if (refused) c.errorEdge else c.linePanel, shape).padding(StretchSpacing.space10),
                    verticalArrangement = Arrangement.spacedBy(StretchSpacing.space4),
                ) {
                    RunText(stringResource(Res.string.dispatch_line, form.workflow!!.fileName, form.repo!!.fullName, form.branch!!.name), RunTextKind.MonoSmall, c.ink, maxLines = 2)
                    val values = form.values().entries.joinToString(SEPARATOR) { "${it.key}=${it.value}" }
                    if (values.isNotEmpty()) RunText(values, RunTextKind.MonoSmall, c.inkMuted, maxLines = 4)
                }
            }
        }
    }
}

private fun workflowReady(form: RunWorkflowState) = form.repo != null && form.workflow != null && form.branch != null && form.spec?.dispatchable == true

@Composable
private fun FormRow(label: String, content: @Composable () -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space14), verticalAlignment = Alignment.CenterVertically) {
        RunText(label, RunTextKind.Body, Stretch.colors.inkControl, Modifier.width(RunSizes.formLabel))
        Column(Modifier.weight(1f)) { content() }
    }
}

private const val SEPARATOR = " · "
