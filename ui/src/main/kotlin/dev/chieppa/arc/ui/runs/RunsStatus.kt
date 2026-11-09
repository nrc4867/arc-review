package dev.chieppa.arc.ui.runs

import androidx.compose.runtime.Composable
import dev.chieppa.arc.core.actions.RunFilter
import dev.chieppa.arc.core.actions.RunGroup
import dev.chieppa.arc.core.actions.RunGroups
import dev.chieppa.arc.core.actions.RunState
import dev.chieppa.arc.core.actions.WorkflowRun
import dev.chieppa.arc.design.ArcIcons
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.run_number_title
import dev.chieppa.arc.design.generated.run_watching
import dev.chieppa.arc.design.generated.runs_rest_budget
import dev.chieppa.arc.design.generated.runs_rules_on
import dev.chieppa.arc.design.generated.runs_status_count
import dev.chieppa.arc.design.generated.runs_status_jobs
import dev.chieppa.arc.design.generated.runs_status_polled
import dev.chieppa.arc.design.generated.runs_status_stalled
import dev.chieppa.arc.design.generated.runs_status_wait
import dev.chieppa.arc.ui.pulls.currentTime
import dev.chieppa.stretch.frame.StatusItem
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

/** The status bar's start for a run filter's tab: the filter, how many runs in how many repos, how many wait on the person, and the REST budget. */
@Composable
fun RunsStatusStart(filter: RunFilter, watch: RunWatch) {
    val now = currentTime()
    val runs = RunGroups.of(watch.runsOf(filter), watch.waitingOnMe, now / RunTimes.MILLIS_PER_SECOND).flatMap { it.runs }
    StatusItem(filter.name, ArcIcons.Runs)
    val repos = runs.map { it.repo.lowercase() }.distinct().size
    StatusItem(pluralStringResource(Res.plurals.runs_status_count, runs.size, runs.size, repos))
    val waiting = RunGroups.of(runs, watch.waitingOnMe, now / RunTimes.MILLIS_PER_SECOND).firstOrNull { it.group == RunGroup.WaitingOnYou }?.runs?.size ?: 0
    if (waiting > 0) StatusItem(stringResource(Res.string.runs_status_wait, waiting))
}

/** The status bar's end for run tabs: the REST budget left, and for a filter how many Mine rules are on. */
@Composable
fun RunsStatusEnd(watch: RunWatch, filter: RunFilter?) {
    watch.budget?.let { StatusItem(stringResource(Res.string.runs_rest_budget, it.remaining, it.limit)) }
    if (filter?.mine == true) StatusItem(stringResource(Res.string.runs_rules_on, watch.watchlist.mineRules.size), ArcIcons.Bell)
}

/** The status bar's start for a run's page: the run, how far its jobs are, how often it is polled. */
@Composable
fun RunStatusStart(run: WorkflowRun, watch: RunWatch) {
    StatusItem(stringResource(Res.string.run_number_title, run.workflowName, run.number), ArcIcons.Runs)
    val jobs = watch.details[run.ref]?.jobs.orEmpty()
    if (jobs.isNotEmpty()) StatusItem(stringResource(Res.string.runs_status_jobs, jobs.count { it.state.finished }, jobs.size))
    if (!run.state.finished) StatusItem(stringResource(Res.string.runs_status_polled, (RunTimes.OPEN_MILLIS / RunTimes.MILLIS_PER_SECOND).toInt()))
    if (watch.stalls[run.ref].orEmpty().isNotEmpty() && run.state == RunState.Running) StatusItem(stringResource(Res.string.runs_status_stalled))
}

/** The status bar's end for a run's page: the REST budget and whether the run is watched. */
@Composable
fun RunStatusEnd(run: WorkflowRun, watch: RunWatch) {
    watch.budget?.let { StatusItem(stringResource(Res.string.runs_rest_budget, it.remaining, it.limit)) }
    if (run.ref in watch.watchlist.runs) StatusItem(stringResource(Res.string.run_watching), dev.chieppa.stretch.theme.StretchIcons.Eye)
}
