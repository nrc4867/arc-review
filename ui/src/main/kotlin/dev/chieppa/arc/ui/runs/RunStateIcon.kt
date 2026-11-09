package dev.chieppa.arc.ui.runs

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.chieppa.arc.core.actions.RunState
import dev.chieppa.arc.design.ArcIcons
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.run_state_cancelled
import dev.chieppa.arc.design.generated.run_state_failed
import dev.chieppa.arc.design.generated.run_state_queued
import dev.chieppa.arc.design.generated.run_state_running
import dev.chieppa.arc.design.generated.run_state_skipped
import dev.chieppa.arc.design.generated.run_state_succeeded
import dev.chieppa.arc.design.generated.run_state_waiting
import dev.chieppa.stretch.theme.Icon
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.ui.Tooltip
import org.jetbrains.compose.resources.stringResource

/** A run's, job's or step's state as one icon in its color, named for screen readers and in a tooltip. */
@Composable
fun RunStateIcon(state: RunState, modifier: Modifier = Modifier, size: Dp = 16.dp) {
    val c = Stretch.colors
    val (icon, color, text) = when (state) {
        RunState.Waiting -> Triple(ArcIcons.CheckWaiting, c.warnInk, Res.string.run_state_waiting)
        RunState.Queued -> Triple(ArcIcons.CheckQueued, c.inkSubtle, Res.string.run_state_queued)
        RunState.Running -> Triple(ArcIcons.CheckPending, c.accent, Res.string.run_state_running)
        RunState.Succeeded -> Triple(ArcIcons.CheckRun, c.deltaUp, Res.string.run_state_succeeded)
        RunState.Failed -> Triple(ArcIcons.CheckFail, c.errorInk, Res.string.run_state_failed)
        RunState.Cancelled -> Triple(ArcIcons.Stop, c.inkMuted, Res.string.run_state_cancelled)
        RunState.Skipped -> Triple(ArcIcons.CheckQueued, c.inkSubtle, Res.string.run_state_skipped)
    }
    val label = stringResource(text)
    Tooltip(label) { Icon(icon, color, modifier, size = size, contentDescription = label) }
}
