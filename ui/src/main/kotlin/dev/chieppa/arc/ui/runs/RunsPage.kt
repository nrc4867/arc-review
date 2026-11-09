package dev.chieppa.arc.ui.runs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import dev.chieppa.arc.core.actions.RunGroup
import dev.chieppa.arc.core.actions.RunGroups
import dev.chieppa.arc.core.actions.timing.TimingKey
import dev.chieppa.arc.core.pane.Page
import dev.chieppa.arc.ui.pulls.currentTime
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.ui.Divider

/**
 * A run filter's tab: its tool row over its runs, grouped into those waiting on the person, running, failed and done
 * today, each group folding under its head. A click on a run opens its page; a right-click offers what else can be
 * done with it. A page whose filter is gone is plain.
 */
@Composable
fun RunsPage(page: Page.Runs, workbench: Workbench, modifier: Modifier = Modifier) {
    val controller = workbench.runs
    val watch = controller.watch
    val filter = watch.watchlist.filter(page.filter) ?: return Box(modifier.fillMaxSize())
    val now = currentTime()
    var repo by remember(filter.id) { mutableStateOf<String?>(null) }
    var folded by remember(filter.id) { mutableStateOf(emptySet<RunGroup>()) }
    val runs = watch.runsOf(filter, repo)
    val groups = RunGroups.of(runs, watch.waitingOnMe, now / RunTimes.MILLIS_PER_SECOND)
    val repos = watch.polledRepos().map { it.fullName }.sortedBy { it.lowercase() }
    Column(modifier.fillMaxSize()) {
        RunsToolRow(filter, watch, repos, repo, { repo = it }) { controller.startDispatch(repo?.let(dev.chieppa.arc.host.github.repo.GitHubRepo::parse)) }
        Divider()
        LazyColumn(Modifier.weight(1f).fillMaxWidth(), rememberLazyListState()) {
            item(HEADER) { RunHeaderRow() }
            for (group in groups) {
                val open = group.group !in folded
                item(GROUP + group.group.name) {
                    RunGroupRow(group.group, group.runs.size, !open) { folded = if (open) folded + group.group else folded - group.group }
                }
                if (open) {
                    items(group.runs, key = { group.group.name + it.ref }) { run ->
                        val detail = watch.details[run.ref]
                        val usual = detail?.jobs?.mapNotNull { watch.timings.jobP90(TimingKey(run.repo, run.workflowPath, it.name)) }?.maxOrNull()
                        RunRow(
                            run = run,
                            why = watch.why(run),
                            jobs = detail?.jobs,
                            login = watch.login,
                            now = now,
                            usual = usual,
                            open = run.ref in watch.open,
                            watched = run.ref in watch.watchlist.runs,
                            waitingOnMe = run.ref in watch.waitingOnMe,
                            hasArtifacts = !detail?.artifacts.isNullOrEmpty(),
                            controller = controller,
                            onOpen = { workbench.panes.openRun(run.ref) },
                        ) { close -> RunMenu(workbench, run, close) }
                    }
                }
            }
        }
    }
}

private const val HEADER = "header"
private const val GROUP = "group:"
