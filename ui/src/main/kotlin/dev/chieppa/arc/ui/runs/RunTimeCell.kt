package dev.chieppa.arc.ui.runs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import dev.chieppa.arc.core.actions.RunState
import dev.chieppa.arc.core.actions.WorkflowRun
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.run_started_ago
import dev.chieppa.arc.design.generated.run_time_none
import dev.chieppa.arc.design.generated.run_waiting
import dev.chieppa.arc.ui.pulls.shortAge
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchSpacing
import org.jetbrains.compose.resources.stringResource

/**
 * How long a run took or has taken, over when it started or finished; a running one shows how far it is through its
 * [usual] time, when there is one.
 */
@Composable
fun RunTimeCell(run: WorkflowRun, now: Long, usual: Long?, modifier: Modifier = Modifier) {
    val c = Stretch.colors
    val seconds = run.seconds(now / RunTimes.MILLIS_PER_SECOND)
    Column(modifier, horizontalAlignment = Alignment.End) {
        RunText(seconds?.let(RunDurations::of) ?: stringResource(Res.string.run_time_none), RunTextKind.MonoSmall, c.inkControl, align = TextAlign.End)
        val under = when (run.state) {
            RunState.Waiting -> if (run.awaitsForkApproval) shortAge(run.createdAt.toEpochMilli(), now) else stringResource(Res.string.run_waiting)
            RunState.Running, RunState.Queued -> stringResource(Res.string.run_started_ago, shortAge((run.startedAt ?: run.createdAt).toEpochMilli(), now))
            else -> shortAge(run.updatedAt.toEpochMilli(), now)
        }
        RunText(under, RunTextKind.Small, c.inkSubtle, align = TextAlign.End)
        if (run.state == RunState.Running && usual != null && usual > 0 && seconds != null) {
            val shape = Stretch.shapes.pillShape
            Box(Modifier.padding(top = StretchSpacing.space4).fillMaxWidth().height(RunSizes.progressBar).clip(shape).background(c.track)) {
                Box(Modifier.fillMaxHeight().fillMaxWidth((seconds.toFloat() / usual).coerceIn(0f, 1f)).background(c.accent))
            }
        }
    }
}
