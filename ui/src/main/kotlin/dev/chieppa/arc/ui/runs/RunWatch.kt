package dev.chieppa.arc.ui.runs

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dev.chieppa.arc.core.actions.MineFacts
import dev.chieppa.arc.core.actions.MineRule
import dev.chieppa.arc.core.actions.MineRules
import dev.chieppa.arc.core.actions.MyPull
import dev.chieppa.arc.core.actions.PendingDeployment
import dev.chieppa.arc.core.actions.RunFilter
import dev.chieppa.arc.core.actions.RunQuery
import dev.chieppa.arc.core.actions.RunRef
import dev.chieppa.arc.core.actions.RunWatchlist
import dev.chieppa.arc.core.actions.WatchedWorkflow
import dev.chieppa.arc.core.actions.WhyMine
import dev.chieppa.arc.core.actions.WorkflowRun
import dev.chieppa.arc.core.actions.timing.RunStall
import dev.chieppa.arc.core.actions.timing.TimingHistory
import dev.chieppa.arc.host.github.pulls.RateBudget
import dev.chieppa.arc.host.github.repo.GitHubRepo
import dev.chieppa.arc.ui.pulls.WatchStatus
import kotlinx.coroutines.channels.Channel

/**
 * The runs the app watches and what it knows of them: the run filters and Mine rules, each polled repo's latest runs,
 * the runs waiting on the person with their deployments, each opened or watched run's jobs, the stalls found, the step
 * timings, and how polling stands. [followed] are the repos the switcher follows; the person's [login] and open pull
 * requests come from the pull request polling. Views read it and recompose when it changes.
 */
class RunWatch(watchlist: RunWatchlist = RunWatchlist(), timings: TimingHistory = TimingHistory(), private val clock: () -> Long = System::currentTimeMillis) {
    var watchlist by mutableStateOf(watchlist)
    var timings by mutableStateOf(timings)
    var lists by mutableStateOf<Map<String, List<WorkflowRun>>>(emptyMap())
    var waitingOnMe by mutableStateOf<Set<RunRef>>(emptySet())
    var pending by mutableStateOf<Map<RunRef, List<PendingDeployment>>>(emptyMap())
    var details by mutableStateOf<Map<RunRef, RunDetail>>(emptyMap())
    var stalls by mutableStateOf<Map<RunRef, List<RunStall>>>(emptyMap())
    var status by mutableStateOf(WatchStatus.Waiting)
    var budget by mutableStateOf<RateBudget?>(null)
    var login by mutableStateOf<String?>(null)
    var myPulls by mutableStateOf<List<MyPull>>(emptyList())
    var followed by mutableStateOf<Set<GitHubRepo>>(emptySet())

    /** The runs whose page shows, polled more often. */
    var open by mutableStateOf<Set<RunRef>>(emptySet())

    /** How many repos poll every minute and how many have gone quiet, as the last poll left them. */
    var activeRepos by mutableIntStateOf(0)
    var quietRepos by mutableIntStateOf(0)

    val refreshRequests = Channel<Unit>(Channel.CONFLATED)

    fun refresh() {
        refreshRequests.trySend(Unit)
    }

    fun now(): Long = clock()

    fun facts(): MineFacts = MineFacts(login, myPulls, waitingOnMe, watchlist.workflows, watchlist.runs.keys)

    /** Every run known: the polled lists, and opened or watched runs found outside them. */
    fun all(): List<WorkflowRun> {
        val listed = lists.values.flatten()
        val ids = listed.map { it.ref }.toSet()
        return listed + details.values.map { it.run }.filter { it.ref !in ids }
    }

    /** The run [ref], as last read. */
    fun run(ref: RunRef): WorkflowRun? = details[ref]?.run ?: lists[ref.repo.lowercase()]?.firstOrNull { it.id == ref.id }

    fun why(run: WorkflowRun): WhyMine? = MineRules.why(run, facts(), watchlist.mineRules)

    /** The runs [filter] shows, in [repo] only when one is given. */
    fun runsOf(filter: RunFilter, repo: String? = null): List<WorkflowRun> {
        val facts = facts()
        val query = RunQuery.parse(filter.query)
        return all().filter { run ->
            (repo == null || run.repo.equals(repo, true)) &&
                if (filter.mine) MineRules.why(run, facts, watchlist.mineRules) != null else query.matches(run, waitingOnMe)
        }
    }

    /** How many known runs [rule] finds, whether or not it is on. */
    fun ruleCount(rule: MineRule): Int = all().count { run -> MineRules.reasons(run, facts(), setOf(rule)).isNotEmpty() }

    /** The repos polled: those followed, those of the person's pull requests, and those named by filters or watches. */
    fun polledRepos(): Set<GitHubRepo> {
        val named = watchlist.filters.flatMap { RunQuery.parse(it.query).repos } +
            myPulls.map { it.repo } + watchlist.workflows.map { it.repo } + watchlist.runs.keys.map { it.repo } + open.map { it.repo }
        return followed + named.mapNotNull(GitHubRepo::parse)
    }

    fun isWatched(run: WorkflowRun): Boolean = MineRules.watched(run, facts())

    /** Watches [run], or stops watching it. */
    fun watchRun(run: RunRef, on: Boolean) {
        watchlist = watchlist.watchingRun(run, on, clock())
    }

    fun watchWorkflow(run: WorkflowRun, on: Boolean) {
        watchlist = watchlist.watchingWorkflow(WatchedWorkflow(run.repo, run.workflowPath), on)
    }

    fun setRule(rule: MineRule, on: Boolean) {
        watchlist = watchlist.withRule(rule, on)
    }

    /** Adds a filter named [name] on [query]; returns it. */
    fun addFilter(name: String, query: String): RunFilter {
        val filter = RunFilter(watchlist.nextId(), name, query.trim())
        watchlist = watchlist.withFilter(filter)
        refresh()
        return filter
    }

    fun updateFilter(filter: RunFilter) {
        watchlist = watchlist.withFilter(filter)
        refresh()
    }

    fun removeFilter(id: String) {
        watchlist = watchlist.withoutFilter(id)
    }
}
