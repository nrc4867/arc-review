package dev.chieppa.arc.ui.runs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.chieppa.arc.core.actions.RunState
import dev.chieppa.arc.core.actions.WhyMine
import dev.chieppa.arc.core.actions.WorkflowRun
import dev.chieppa.arc.core.actions.timing.RunStall
import dev.chieppa.arc.design.ArcIcons
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.run_by
import dev.chieppa.arc.design.generated.run_cancel_run
import dev.chieppa.arc.design.generated.run_finished_in
import dev.chieppa.arc.design.generated.run_menu
import dev.chieppa.arc.design.generated.run_number
import dev.chieppa.arc.design.generated.run_open_branch
import dev.chieppa.arc.design.generated.run_open_commit
import dev.chieppa.arc.design.generated.run_rerun_failed
import dev.chieppa.arc.design.generated.run_rerun
import dev.chieppa.arc.design.generated.run_stalled
import dev.chieppa.arc.design.generated.run_started_long_ago
import dev.chieppa.arc.design.generated.run_view_jobs
import dev.chieppa.arc.design.generated.run_view_summary
import dev.chieppa.arc.design.generated.run_watch
import dev.chieppa.arc.design.generated.run_watching
import dev.chieppa.arc.ui.pulls.SmallAvatar
import dev.chieppa.arc.ui.pulls.currentTime
import dev.chieppa.arc.ui.pulls.shortAge
import dev.chieppa.arc.ui.util.ArcSizes
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.arc.ui.workbench.openRunCommit
import dev.chieppa.stretch.frame.Menu
import dev.chieppa.stretch.theme.Icon
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchIcons
import dev.chieppa.stretch.theme.StretchSpacing
import dev.chieppa.stretch.ui.Button
import dev.chieppa.stretch.ui.ButtonKind
import dev.chieppa.stretch.ui.IconButton
import dev.chieppa.stretch.ui.Segmented
import dev.chieppa.stretch.ui.Tooltip
import org.jetbrains.compose.resources.stringResource

/**
 * The top of a run's page: its state, workflow, number, why it is the person's and whether it stalled, over its repo,
 * ref, trigger, when it ran and its workflow file; then Summary or Jobs once it is done, re-run or cancel, opening its
 * commit, watching, and its menu.
 */
@Composable
fun RunHeader(run: WorkflowRun, why: WhyMine?, stalls: List<RunStall>, state: RunPageState, workbench: Workbench) {
    val c = Stretch.colors
    val controller = workbench.runs
    val now = currentTime()
    var menu by remember { mutableStateOf(false) }
    val summary = stringResource(Res.string.run_view_summary)
    val jobs = stringResource(Res.string.run_view_jobs)
    Row(
        Modifier.fillMaxWidth().heightIn(min = RunSizes.toolRow).background(c.surfacePanel).padding(horizontal = StretchSpacing.space12, vertical = StretchSpacing.space6),
        horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space10),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RunStateIcon(run.state, size = ArcSizes.pullIcon)
        Column(Modifier.weight(1f)) {
            Row(horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space8), verticalAlignment = Alignment.CenterVertically) {
                RunText(run.workflowName, RunTextKind.Heading, c.inkStrong, Modifier.weight(1f, fill = false))
                RunText(stringResource(Res.string.run_number, run.number), RunTextKind.MonoSmall, c.inkMuted)
                why?.let { WhyChip(it) }
                if (stalls.isNotEmpty()) WhyPill(ArcIcons.CheckWaiting, stringResource(Res.string.run_stalled), WhyTone.Warn)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space6), verticalAlignment = Alignment.CenterVertically) {
                RunText(run.repo, RunTextKind.MonoSmall, c.inkMuted)
                Dot()
                Icon(ArcIcons.Branch, c.inkMuted, size = ArcSizes.smallIcon)
                RunText(runRefText(run), RunTextKind.MonoSmall, c.inkMuted)
                Dot()
                RunText(stringResource(Res.string.run_by, run.event), RunTextKind.Small, c.inkMuted)
                (run.triggeringActor ?: run.actor)?.let { SmallAvatar(it); RunText(it, RunTextKind.Small, c.inkMuted) }
                Dot()
                val seconds = run.seconds(now / RunTimes.MILLIS_PER_SECOND)
                val time = if (run.state.finished && seconds != null) {
                    stringResource(Res.string.run_finished_in, shortAge(run.updatedAt.toEpochMilli(), now), RunDurations.of(seconds))
                } else {
                    stringResource(Res.string.run_started_long_ago, shortAge((run.startedAt ?: run.createdAt).toEpochMilli(), now))
                }
                RunText(time, RunTextKind.Small, c.inkMuted)
                Dot()
                RunText(run.workflowPath, RunTextKind.Small, c.inkMuted, Modifier.weight(1f, fill = false))
            }
        }
        if (run.state.finished) {
            Segmented(
                RunView.entries,
                state.view,
                { state.view = it },
                { if (it == RunView.Summary) summary else jobs },
                mode = true,
            )
        }
        when (run.state) {
            RunState.Failed -> Button(stringResource(Res.string.run_rerun_failed), { controller.ask(RunConfirm.Rerun(run, failedOnly = true)) }, kind = ButtonKind.Quiet, icon = ArcIcons.Refresh)
            RunState.Succeeded, RunState.Cancelled, RunState.Skipped ->
                Button(stringResource(Res.string.run_rerun), { controller.ask(RunConfirm.Rerun(run, failedOnly = false)) }, kind = ButtonKind.Quiet, icon = ArcIcons.Refresh)
            else -> Unit
        }
        val branch = run.pulls.isEmpty() && !isTag(run) && run.event == WORKFLOW_DISPATCH
        Button(stringResource(if (branch) Res.string.run_open_branch else Res.string.run_open_commit), { workbench.openRunCommit(run) }, kind = ButtonKind.Quiet, icon = StretchIcons.Layers)
        if (!run.state.finished) Button(stringResource(Res.string.run_cancel_run), { controller.ask(RunConfirm.Cancel(run)) }, kind = ButtonKind.Quiet, icon = ArcIcons.Stop)
        val watched = run.ref in controller.watch.watchlist.runs
        val watchLabel = stringResource(if (watched) Res.string.run_watching else Res.string.run_watch)
        Tooltip(watchLabel) { IconButton(StretchIcons.Eye, watchLabel, { controller.toggleWatch(run) }, quiet = !watched, on = watched) }
        Box {
            val label = stringResource(Res.string.run_menu)
            Tooltip(label) { IconButton(StretchIcons.More, label, { menu = true }) }
            Menu(menu, { menu = false }) { RunMenu(workbench, run) { menu = false } }
        }
    }
}

@Composable
private fun Dot() {
    Box(Modifier.size(ArcSizes.separatorDot).background(Stretch.colors.inkSubtle, CircleShape))
}

private const val WORKFLOW_DISPATCH = "workflow_dispatch"
