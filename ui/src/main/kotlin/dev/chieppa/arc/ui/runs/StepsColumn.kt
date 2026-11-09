package dev.chieppa.arc.ui.runs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.chieppa.arc.core.actions.RunJob
import dev.chieppa.arc.core.actions.RunState
import dev.chieppa.arc.core.actions.WorkflowRun
import dev.chieppa.arc.core.actions.timing.RunStall
import dev.chieppa.arc.core.actions.timing.TimingKey
import dev.chieppa.arc.design.ArcIcons
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.run_artifacts
import dev.chieppa.arc.design.generated.run_job_started
import dev.chieppa.arc.design.generated.run_stalled_over
import dev.chieppa.arc.design.generated.run_step
import dev.chieppa.arc.design.generated.run_step_against
import dev.chieppa.arc.design.generated.run_step_elapsed
import dev.chieppa.arc.design.generated.run_step_usual
import dev.chieppa.arc.ui.pulls.shortAge
import dev.chieppa.arc.ui.util.ArcSizes
import dev.chieppa.arc.ui.util.bottomRule
import dev.chieppa.stretch.theme.Icon
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchSpacing
import org.jetbrains.compose.resources.stringResource

/**
 * The picked job's steps: its name and runner, then each step with its time; while the job runs, each step's time is
 * held against its usual 90th percentile, and a stalled step is marked with how far over it is. A finished run's
 * artifacts follow. A click on a step shows its log.
 */
@Composable
fun StepsColumn(run: WorkflowRun, job: RunJob, detail: RunDetail?, state: RunPageState, watch: RunWatch, stalls: List<RunStall>, now: Long, modifier: Modifier = Modifier) {
    val c = Stretch.colors
    val running = job.state == RunState.Running
    val key = TimingKey(run.repo, run.workflowPath, job.name)
    val nowSeconds = now / RunTimes.MILLIS_PER_SECOND
    Column(modifier.background(c.surfacePanel).verticalScroll(rememberScrollState())) {
        val runner = (job.labels.firstOrNull() ?: job.runner).orEmpty()
        val head = if (running && job.startedAt != null) "$runner · ${stringResource(Res.string.run_job_started, shortAge(job.startedAt!!.toEpochMilli(), now))}" else runner
        PaneHead(ArcIcons.Runs, job.name, head)
        if (running) {
            Row(
                Modifier.fillMaxWidth().height(RunSizes.step).bottomRule(c.chartGrid).padding(horizontal = StretchSpacing.space14),
                horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space10),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RunText("", RunTextKind.Eyebrow, c.inkMuted, Modifier.padding(start = ArcSizes.pullIcon))
                RunText(stringResource(Res.string.run_step), RunTextKind.Eyebrow, c.inkMuted, Modifier.weight(1f))
                RunText(stringResource(Res.string.run_step_against), RunTextKind.Eyebrow, c.inkMuted, Modifier.padding(end = StretchSpacing.space4))
                RunText(stringResource(Res.string.run_step_elapsed), RunTextKind.Eyebrow, c.inkMuted)
                RunText(stringResource(Res.string.run_step_usual), RunTextKind.Eyebrow, c.inkMuted)
            }
        }
        for (step in job.steps) {
            val stall = stalls.firstOrNull { it.jobId == job.id && it.step == step.number }
            if (running) {
                TimedStepRow(step, nowSeconds, watch.timings.stepP90(key, step.name), stall != null, state.step == step.number) { state.step = step.number }
            } else {
                StepRow(step, nowSeconds, state.step == step.number) { state.step = step.number }
            }
        }
        stalls.firstOrNull { it.jobId == job.id }?.let { stall ->
            Row(Modifier.padding(StretchSpacing.space12)) {
                WhyPill(ArcIcons.CheckWaiting, stringResource(Res.string.run_stalled_over, RunDurations.minutes(stall.overSeconds)), WhyTone.Warn)
            }
        }
        val artifacts = detail?.artifacts.orEmpty()
        if (artifacts.isNotEmpty()) {
            Column(Modifier.padding(StretchSpacing.space12), verticalArrangement = Arrangement.spacedBy(StretchSpacing.space6)) {
                RunText(stringResource(Res.string.run_artifacts), RunTextKind.Eyebrow, c.inkMuted)
                for (artifact in artifacts) {
                    Row(horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space8), verticalAlignment = Alignment.CenterVertically) {
                        Icon(ArcIcons.Artifact, c.inkMuted, size = ArcSizes.smallIcon)
                        RunText(artifact.name, RunTextKind.Body, c.ink, Modifier.weight(1f))
                        RunText(Sizes.bytes(artifact.sizeBytes), RunTextKind.MonoSmall, c.inkMuted)
                    }
                }
            }
        }
    }
}
