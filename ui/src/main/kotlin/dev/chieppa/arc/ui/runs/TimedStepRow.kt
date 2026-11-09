package dev.chieppa.arc.ui.runs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import dev.chieppa.arc.core.actions.RunState
import dev.chieppa.arc.core.actions.RunStep
import dev.chieppa.arc.ui.util.bottomRule
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchSpacing

/**
 * One step of a running job: its state and name, a bar of its time against its usual 90th percentile, its elapsed
 * time and the usual one. A stalled step is washed and edged in the warning colors; one not started is quiet.
 */
@Composable
fun TimedStepRow(step: RunStep, now: Long, usual: Long?, stalled: Boolean, selected: Boolean, onClick: () -> Unit) {
    val c = Stretch.colors
    val idle = step.state == RunState.Queued
    val edge = RunSizes.selectedEdge
    Row(
        Modifier.fillMaxWidth().height(RunSizes.step)
            .background(if (stalled) c.warnWash else if (selected) c.onFill else Color.Transparent)
            .drawBehind { if (stalled) drawLine(c.warnEdge, Offset(edge.toPx() / 2, 0f), Offset(edge.toPx() / 2, size.height), edge.toPx()) }
            .bottomRule(c.chartGrid).clickable(role = Role.Button, onClick = onClick).padding(horizontal = StretchSpacing.space14),
        horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space10),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RunStateIcon(step.state)
        RunText(step.name, RunTextKind.Body, if (idle) c.inkSubtle else c.ink, Modifier.weight(1f))
        val elapsed = step.seconds(now)
        TimingBar(elapsed, usual, step.state, stalled)
        RunText(elapsed?.let(RunDurations::of).orEmpty(), RunTextKind.MonoSmall, c.inkMuted, Modifier.width(RunSizes.elapsedColumn), TextAlign.End)
        RunText(usual?.let(RunDurations::of).orEmpty(), RunTextKind.Small, c.inkSubtle, Modifier.width(RunSizes.usualColumn), TextAlign.End)
    }
}
