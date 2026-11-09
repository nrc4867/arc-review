package dev.chieppa.arc.ui.runs

import dev.chieppa.arc.core.actions.MineRules
import dev.chieppa.arc.core.actions.RunRef
import dev.chieppa.arc.core.actions.RunState
import dev.chieppa.arc.core.actions.timing.RunStall
import dev.chieppa.arc.core.actions.timing.StallFinder
import dev.chieppa.arc.core.actions.timing.TimingKey
import dev.chieppa.arc.host.github.api.GitHubException
import dev.chieppa.arc.host.github.repo.GitHubRepo
import dev.chieppa.arc.ui.services.ActionsService
import dev.chieppa.arc.ui.services.RunKeeper

/**
 * Reads runs' jobs for [watch]: every [RunTimes.OPEN_MILLIS] while a run's page shows, every
 * [RunTimes.WATCHED_MILLIS] while it is watched or the person's own and running. A job that finishes has its log read
 * once through the log filter, with its annotations; a finished run has its artifacts read once. Successful jobs add
 * to the step timings, running ones are checked for stalls, and what is kept goes to [keeper].
 */
class RunDetails(
    private val watch: RunWatch,
    private val service: ActionsService,
    private val notifier: RunNotifier,
    private val keeper: RunKeeper,
) {
    private val due = mutableMapOf<RunRef, Long>()
    private val told = mutableMapOf<RunRef, List<RunStall>>()

    /** Reads [ref] at the next poll. */
    fun markDue(ref: RunRef) {
        due[ref] = 0
    }

    /** Reads every run that is due. */
    suspend fun pollDue() {
        if (!service.hasToken()) return
        val now = watch.now()
        val facts = watch.facts()
        val followed = watch.all().filter { run ->
            !run.state.finished && MineRules.reasons(run, facts, watch.watchlist.mineRules).isNotEmpty()
        }.map { it.ref }
        for (ref in watch.open + followed) {
            if ((due[ref] ?: 0) > now) continue
            due[ref] = now + if (ref in watch.open) RunTimes.OPEN_MILLIS else RunTimes.WATCHED_MILLIS
            try {
                read(ref)
            } catch (_: GitHubException) {
            }
        }
    }

    /** Reads run [ref] now: the run when no list holds it, its jobs, and what finished since. */
    suspend fun read(ref: RunRef) {
        val repo = GitHubRepo.parse(ref.repo) ?: return
        val now = watch.now()
        val known = watch.details[ref] ?: keeper.load(ref)?.let(RunDetail::of)
        val listed = watch.lists[ref.repo.lowercase()]?.firstOrNull { it.id == ref.id }
        val run = listed ?: service.run(repo, ref.id) ?: known?.run ?: return
        var detail = (known ?: RunDetail(run)).copy(run = run)
        val page = service.jobs(repo, ref.id, detail.jobsEtag)
        val jobs = page.value ?: detail.jobs
        detail = detail.copy(jobs = jobs, jobsEtag = page.etag, readAt = now)
        var finishedSomething = false
        for (job in jobs.filter { it.state.finished && it.id !in detail.logs }) {
            val log = service.keptLog(repo, job) ?: continue
            detail = detail.copy(logs = detail.logs + (job.id to log), annotations = detail.annotations + service.annotations(repo, job))
            finishedSomething = true
        }
        if (run.state.finished && detail.artifacts == null) {
            detail = detail.copy(artifacts = service.artifacts(repo, run.id))
            finishedSomething = true
        }
        if (detail.spec == null) detail = detail.copy(spec = service.workflowSpec(repo, run.workflowPath, run.headSha))
        var timings = watch.timings
        jobs.filter { it.state == RunState.Succeeded }.forEach { timings = timings.record(TimingKey(run.repo, run.workflowPath, it.name), run.id, it) }
        if (timings != watch.timings) watch.timings = timings
        watch.details = watch.details + (ref to detail)
        val found = if (run.state.finished) emptyList() else StallFinder.of(run, jobs, watch.timings, now / RunTimes.MILLIS_PER_SECOND)
        RunEvents.stalled(run, found, told[ref].orEmpty(), watch.facts(), watch.watchlist.notify).forEach { notifier.tell(it, now) }
        told[ref] = (told[ref].orEmpty() + found).distinctBy { it.jobId to it.step }
        watch.stalls = if (found.isEmpty()) watch.stalls - ref else watch.stalls + (ref to found)
        if (finishedSomething) keeper.save(detail.record(now))
    }
}
