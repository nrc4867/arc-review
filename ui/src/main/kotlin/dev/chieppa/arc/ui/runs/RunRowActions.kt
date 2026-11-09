package dev.chieppa.arc.ui.runs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.chieppa.arc.core.actions.RunState
import dev.chieppa.arc.core.actions.WorkflowRun
import dev.chieppa.arc.design.ArcIcons
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.run_artifacts
import dev.chieppa.arc.design.generated.run_cancel
import dev.chieppa.arc.design.generated.run_open
import dev.chieppa.arc.design.generated.run_rerun
import dev.chieppa.arc.design.generated.run_rerun_failed
import dev.chieppa.arc.design.generated.run_review
import dev.chieppa.arc.design.generated.run_watch
import dev.chieppa.arc.design.generated.run_watching
import dev.chieppa.arc.ui.util.TipIconButton
import dev.chieppa.stretch.theme.StretchIcons
import dev.chieppa.stretch.theme.StretchSpacing
import dev.chieppa.stretch.ui.Button
import dev.chieppa.stretch.ui.ButtonKind
import org.jetbrains.compose.resources.stringResource

/**
 * The buttons at a run row's end, by its state: Review for one waiting on the person; watch and cancel while it
 * runs; Re-run failed once it failed; artifacts and re-run once it is done; and always open.
 */
@Composable
fun RunRowActions(run: WorkflowRun, waitingOnMe: Boolean, watched: Boolean, hasArtifacts: Boolean, controller: RunController, onOpen: () -> Unit, modifier: Modifier = Modifier) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space4, Alignment.End), verticalAlignment = Alignment.CenterVertically) {
        val watch = { controller.toggleWatch(run) }
        when {
            waitingOnMe -> {
                Button(stringResource(Res.string.run_review), { if (run.awaitsForkApproval) controller.ask(RunConfirm.ApproveFork(run)) else onOpen() }, kind = ButtonKind.Primary, icon = ArcIcons.Gate)
                WatchButton(watched, watch)
            }
            run.state == RunState.Running || run.state == RunState.Queued || run.state == RunState.Waiting -> {
                WatchButton(watched, watch)
                TipIconButton(ArcIcons.Stop, stringResource(Res.string.run_cancel), { controller.ask(RunConfirm.Cancel(run)) })
                TipIconButton(ArcIcons.TearOut, stringResource(Res.string.run_open), onOpen)
            }
            run.state == RunState.Failed -> {
                Button(stringResource(Res.string.run_rerun_failed), { controller.ask(RunConfirm.Rerun(run, failedOnly = true)) }, kind = ButtonKind.Quiet, icon = ArcIcons.Refresh)
                TipIconButton(ArcIcons.TearOut, stringResource(Res.string.run_open), onOpen)
            }
            else -> {
                if (hasArtifacts) TipIconButton(ArcIcons.Artifact, stringResource(Res.string.run_artifacts), onOpen)
                TipIconButton(ArcIcons.Refresh, stringResource(Res.string.run_rerun), { controller.ask(RunConfirm.Rerun(run, failedOnly = false)) })
                TipIconButton(ArcIcons.TearOut, stringResource(Res.string.run_open), onOpen)
            }
        }
    }
}

@Composable
private fun WatchButton(watched: Boolean, onClick: () -> Unit) {
    TipIconButton(StretchIcons.Eye, stringResource(if (watched) Res.string.run_watching else Res.string.run_watch), onClick, on = watched)
}
