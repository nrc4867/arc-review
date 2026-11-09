package dev.chieppa.arc.ui.runs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import dev.chieppa.arc.core.actions.RunJob
import dev.chieppa.arc.core.actions.RunState
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.run_job_gate
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchSpacing
import org.jetbrains.compose.resources.stringResource

/**
 * One job in a run's job graph: its state, name and time, `gate` while it waits at an environment. The picked job has
 * an accent ring, a stalled one the warning colors, and one not started yet a quiet edge.
 */
@Composable
fun JobNode(job: RunJob, selected: Boolean, stalled: Boolean, now: Long, onClick: () -> Unit) {
    val c = Stretch.colors
    val shape = Stretch.shapes.ctlShape
    val waiting = job.state == RunState.Waiting
    val idle = job.state == RunState.Queued
    val (fill, edge) = when {
        stalled -> c.warnWash to c.warnEdge
        waiting -> c.warnWash to c.warnEdge
        selected -> c.surfacePanel to c.accent
        else -> c.surfacePanel to c.lineStrong
    }
    val width: Dp = if (selected || stalled) Stretch.stroke * 2 else Stretch.stroke
    Row(
        Modifier.width(RunSizes.node).height(RunSizes.nodeHeight).clip(shape).background(fill).border(width, edge, shape)
            .clickable(role = Role.Button, onClick = onClick).semantics { this.selected = selected }
            .padding(horizontal = StretchSpacing.space10),
        horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space8),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RunStateIcon(job.state)
        RunText(job.name, RunTextKind.Strong, if (idle) c.inkSubtle else c.inkStrong, Modifier.weight(1f))
        val time = if (waiting) stringResource(Res.string.run_job_gate) else job.seconds(now / RunTimes.MILLIS_PER_SECOND)?.let(RunDurations::of).orEmpty()
        RunText(time, RunTextKind.MonoSmall, c.inkMuted, align = TextAlign.End)
    }
}
