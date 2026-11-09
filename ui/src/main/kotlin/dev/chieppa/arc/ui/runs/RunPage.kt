package dev.chieppa.arc.ui.runs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import dev.chieppa.arc.core.actions.RunState
import dev.chieppa.arc.core.actions.graph.JobColumns
import dev.chieppa.arc.core.pane.Page
import dev.chieppa.arc.host.github.api.GitHubException
import dev.chieppa.arc.ui.pulls.currentTime
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.ui.Divider

/**
 * A run's page: its header, its jobs laid out by what they wait for, and either the finished run's summary beside its
 * artifacts, or the picked job's steps beside the picked step's kept log, with the deployment review beside them while
 * the run waits at an environment the person may approve. While it shows, the run is polled often.
 */
@Composable
fun RunPage(page: Page.Run, workbench: Workbench, modifier: Modifier = Modifier) {
    val controller = workbench.runs
    val watch = controller.watch
    val ref = page.run
    DisposableEffect(ref) {
        watch.open = watch.open + ref
        onDispose { watch.open = watch.open - ref }
    }
    LaunchedEffect(ref) {
        try {
            workbench.runPoller.details.read(ref)
        } catch (_: GitHubException) {
        }
    }
    val state = remember(ref) { RunPageState() }
    val now = currentTime()
    val detail = watch.details[ref]
    val run = detail?.run ?: watch.run(ref) ?: return Box(modifier.fillMaxSize())
    val jobs = detail?.jobs.orEmpty()
    val job = jobs.firstOrNull { it.id == state.job } ?: jobs.firstOrNull { it.state == RunState.Running || it.state == RunState.Failed || it.state == RunState.Waiting } ?: jobs.firstOrNull()
    val stalls = watch.stalls[ref].orEmpty()
    val pending = watch.pending[ref].orEmpty().filter { it.canApprove }
    Column(modifier.fillMaxSize()) {
        RunHeader(run, watch.why(run), stalls, state, workbench)
        Divider()
        JobGraph(JobColumns.of(jobs, detail?.spec?.jobs.orEmpty()), job?.id, stalls, now) { state.job = it.id; state.step = null; state.view = RunView.Jobs }
        Divider()
        if (run.state.finished && state.view == RunView.Summary) {
            Row(Modifier.weight(1f).fillMaxWidth()) {
                RunSummary(run, detail, workbench, Modifier.weight(1f).fillMaxHeight())
                ArtifactsColumn(run, detail, state, workbench, Modifier.width(RunSizes.artifactsColumn).fillMaxHeight())
            }
        } else {
            Row(Modifier.weight(1f).fillMaxWidth()) {
                val running = job?.state == RunState.Running
                if (job != null) {
                    StepsColumn(run, job, detail, state, watch, stalls, now, Modifier.width(if (running) RunSizes.timedStepsColumn else RunSizes.stepsColumn).fillMaxHeight())
                }
                LogColumn(run, job, detail?.logs?.get(job?.id), state, workbench, Modifier.weight(1f).fillMaxHeight())
                if (pending.isNotEmpty()) DeploymentCard(run, pending, workbench, Modifier.width(RunSizes.sideColumn).fillMaxHeight())
            }
        }
    }
}
