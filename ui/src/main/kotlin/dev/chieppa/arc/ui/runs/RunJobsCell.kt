package dev.chieppa.arc.ui.runs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.chieppa.arc.core.actions.RunJob
import dev.chieppa.arc.core.actions.RunState
import dev.chieppa.arc.core.actions.WorkflowRun
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.run_jobs_all
import dev.chieppa.arc.design.generated.run_jobs_done
import dev.chieppa.arc.design.generated.run_jobs_failed
import dev.chieppa.arc.design.generated.run_jobs_gate
import dev.chieppa.arc.design.generated.run_jobs_job
import dev.chieppa.arc.design.generated.run_jobs_one_failed
import dev.chieppa.arc.design.generated.run_jobs_not_started
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchSpacing
import org.jetbrains.compose.resources.stringResource

/** A run's jobs as a strip with how far they got: `3 of 4 · gate`, `job 3 of 5`, `lint failed`. Nothing while its jobs are unread. */
@Composable
fun RunJobsCell(run: WorkflowRun, jobs: List<RunJob>?, modifier: Modifier = Modifier) {
    val c = Stretch.colors
    Column(modifier, verticalArrangement = Arrangement.spacedBy(StretchSpacing.space2)) {
        if (jobs == null) {
            if (run.state == RunState.Waiting && run.awaitsForkApproval) RunText(stringResource(Res.string.run_jobs_not_started), RunTextKind.Small, c.inkSubtle)
            return@Column
        }
        val states = jobs.map { it.state }
        JobStrip(states)
        val done = states.count { it.finished }
        val failed = jobs.filter { it.state == RunState.Failed }
        val text = when {
            jobs.isEmpty() -> stringResource(Res.string.run_jobs_not_started)
            run.state == RunState.Waiting -> stringResource(Res.string.run_jobs_gate, done, jobs.size)
            failed.size == 1 && run.state.finished -> stringResource(Res.string.run_jobs_one_failed, failed.single().name)
            failed.isNotEmpty() -> stringResource(Res.string.run_jobs_failed, failed.size, jobs.size)
            run.state.finished -> stringResource(Res.string.run_jobs_all, done, jobs.size)
            states.count { it == RunState.Running } == 1 -> stringResource(Res.string.run_jobs_job, states.indexOf(RunState.Running) + 1, jobs.size)
            else -> stringResource(Res.string.run_jobs_done, done, jobs.size)
        }
        RunText(text, RunTextKind.Small, c.inkSubtle)
    }
}
