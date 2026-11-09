package dev.chieppa.arc.ui.runs

import dev.chieppa.arc.core.actions.RunRef
import dev.chieppa.arc.core.actions.RunState
import dev.chieppa.arc.core.actions.WorkflowRun
import dev.chieppa.arc.host.github.api.GitHubException
import dev.chieppa.arc.host.github.repo.GitHubRepo
import dev.chieppa.arc.ui.pulls.PullWatch
import dev.chieppa.arc.ui.pulls.WatchStatus
import dev.chieppa.arc.ui.services.ActionsService
import dev.chieppa.arc.ui.services.RunKeeper
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.time.Instant

/**
 * Polls GitHub Actions for [watch] while it runs. GitHub has no list of runs across repos, so each polled repo's
 * latest runs are asked for in turn, conditionally, every [RunTimes.ACTIVE_MILLIS], or [RunTimes.QUIET_MILLIS] once a
 * repo has had no run for a day; the runs waiting at a deployment are asked who may approve them. Jobs are polled
 * through [details]. What a poll sets off goes to [notifier]. The person and their pull requests come from [pulls].
 */
class RunPoller(
    private val watch: RunWatch,
    private val pulls: PullWatch,
    private val service: ActionsService,
    private val notifier: RunNotifier,
    private val keeper: RunKeeper = RunKeeper.NONE,
) {
    private val etags = mutableMapOf<String, String?>()
    private val due = mutableMapOf<String, Long>()
    private val polledAt = mutableMapOf<String, Long>()
    private val pushable = mutableMapOf<String, Boolean>()
    private val pendingAt = mutableMapOf<RunRef, Instant>()
    private var waitingKnown = false
    private var pruned = false

    /** Polls the runs' jobs while their pages show or they are watched. */
    val details = RunDetails(watch, service, notifier, keeper)

    /** Polls until cancelled. */
    suspend fun run() = coroutineScope {
        launch {
            while (true) {
                pollLists()
                withTimeoutOrNull(RunTimes.TICK_MILLIS) { watch.refreshRequests.receive() }?.let { due.clear() }
            }
        }
        launch {
            while (true) {
                details.pollDue()
                delay(RunTimes.OPEN_MILLIS)
            }
        }
    }

    /** One poll of every repo that is due. */
    suspend fun pollLists() {
        if (!service.hasToken()) {
            watch.status = WatchStatus.Unavailable
            return
        }
        pulls.viewer?.login?.let { watch.login = it }
        watch.myPulls = RunSearches.pulls(pulls.results)
        val now = watch.now()
        val repos = watch.polledRepos().associateBy { it.fullName.lowercase() }
        var failed = false
        for ((key, repo) in repos) {
            if ((due[key] ?: 0) > now) continue
            failed = !pollRepo(key, repo, now) || failed
        }
        watch.lists = watch.lists.filterKeys { it in repos }
        val quiet = repos.keys.count { key -> watch.lists[key].orEmpty().none { now / RunTimes.MILLIS_PER_SECOND - it.updatedAt.epochSecond < RunTimes.QUIET_AFTER_SECONDS } }
        watch.quietRepos = quiet
        watch.activeRepos = repos.size - quiet
        try {
            updateWaiting()
        } catch (_: GitHubException) {
            failed = true
        }
        watch.budget = service.budget ?: watch.budget
        watch.status = if (failed) WatchStatus.Failed else WatchStatus.Live
        if (!pruned && !failed) {
            pruned = true
            watch.watchlist = watch.watchlist.forgetRunsBefore(now - RunTimes.FORGET_WATCHED_MILLIS)
            keeper.prune(watch.all().map { it.ref }.toSet(), now)
        }
    }

    private suspend fun pollRepo(key: String, repo: GitHubRepo, now: Long): Boolean {
        val page = try {
            service.runs(repo, etags[key])
        } catch (e: GitHubException) {
            if (e.reason != GitHubException.Reason.Failed) watch.status = WatchStatus.Unavailable
            return false
        }
        etags[key] = page.etag
        val runs = page.value
        if (runs != null) {
            val before = watch.lists[key]
            watch.lists = watch.lists + (key to runs)
            val events = RunEvents.finished(before, runs, watch.facts(), watch.watchlist.notify, polledAt[key] ?: now)
            events.forEach { notifier.tell(it, now) }
            runs.filter { it.state.finished && it.ref in watch.details }.forEach { details.markDue(it.ref) }
        }
        polledAt[key] = now
        val quiet = watch.lists[key].orEmpty().none { now / RunTimes.MILLIS_PER_SECOND - it.updatedAt.epochSecond < RunTimes.QUIET_AFTER_SECONDS }
        due[key] = now + if (quiet) RunTimes.QUIET_MILLIS else RunTimes.ACTIVE_MILLIS
        return true
    }

    /** Finds which waiting runs wait on the person: deployments they may approve, and fork runs where they may push. */
    private suspend fun updateWaiting() {
        val runs = watch.all()
        val waiting = runs.filter { it.state == RunState.Waiting }
        val pending = waiting.filterNot { it.awaitsForkApproval }.associate { run ->
            val repo = GitHubRepo.parse(run.repo) ?: return@associate run.ref to emptyList()
            val kept = watch.pending[run.ref]?.takeIf { pendingAt[run.ref] == run.updatedAt }
            run.ref to (kept ?: service.pendingDeployments(repo, run.id).also { pendingAt[run.ref] = run.updatedAt })
        }
        val forks = waiting.filter { it.awaitsForkApproval && canPush(it) }.map { it.ref }
        val mine = pending.filterValues { list -> list.any { it.canApprove } }.keys + forks
        val events = RunEvents.waiting(if (waitingKnown) watch.waitingOnMe else null, mine, runs, watch.watchlist.notify)
        watch.pending = pending
        watch.waitingOnMe = mine
        waitingKnown = true
        events.forEach { notifier.tell(it, watch.now()) }
    }

    private suspend fun canPush(run: WorkflowRun): Boolean {
        val key = run.repo.lowercase()
        pushable[key]?.let { return it }
        val repo = GitHubRepo.parse(run.repo) ?: return false
        return service.canPush(repo).also { pushable[key] = it }
    }
}
