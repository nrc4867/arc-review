package dev.chieppa.arc.ui.runs

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import dev.chieppa.arc.core.actions.RunArtifact
import dev.chieppa.arc.core.actions.RunJob
import dev.chieppa.arc.core.actions.RunRef
import dev.chieppa.arc.core.actions.RunState
import dev.chieppa.arc.core.actions.WorkflowRun
import dev.chieppa.arc.host.github.api.GitHubException
import dev.chieppa.arc.host.github.repo.GitHubRepo
import dev.chieppa.arc.ui.services.ActionsService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * What a person does to runs: each write waits in [confirming] until they confirm it, then goes to [service], and
 * the run is watched and read again. A write GitHub refuses stays in [confirming] with [failed] set. [dispatching]
 * holds the Run workflow form while it is open. [openRun] opens a run's page.
 */
class RunController(
    val watch: RunWatch,
    private val service: ActionsService,
    private val scope: CoroutineScope,
    private val openRun: (RunRef) -> Unit,
) {
    var confirming by mutableStateOf<RunConfirm?>(null)
    var failed by mutableStateOf(false)
    var busy by mutableStateOf(false)
    var debug by mutableStateOf(false)
    var dispatching by mutableStateOf<RunWorkflowState?>(null)

    /** Asks to confirm [confirm]; a fork approval reads what the pull request changes under `.github/` first. */
    fun ask(confirm: RunConfirm) {
        failed = false
        debug = false
        confirming = confirm
        if (confirm is RunConfirm.ApproveFork) {
            val run = confirm.run
            scope.launch {
                val repo = GitHubRepo.parse(run.repo)
                val owner = run.headOwner
                val branch = run.headBranch
                val changes = if (repo == null || owner == null || branch == null) null else attempt { service.forkChanges(repo, owner, branch) }
                if (confirming == confirm) confirming = confirm.copy(changes = changes, reading = false)
            }
        }
    }

    fun dismiss() {
        confirming = null
        failed = false
    }

    /** Sends the write being confirmed. */
    fun confirm() {
        val confirm = confirming ?: return
        val repo = GitHubRepo.parse(confirm.run.repo) ?: return
        busy = true
        scope.launch {
            val done = attempt {
                when (confirm) {
                    is RunConfirm.Rerun -> service.rerun(repo, confirm.run.id, confirm.failedOnly, debug)
                    is RunConfirm.RerunJob -> service.rerunJob(repo, confirm.job.id, debug)
                    is RunConfirm.Cancel -> {
                        service.cancel(repo, confirm.run.id)
                        confirm.job?.let { job -> scope.launch { rerunWhenStopped(repo, confirm.run.ref, job) } }
                    }
                    is RunConfirm.ApproveFork -> service.approveFork(repo, confirm.run.id)
                }
            } != null
            busy = false
            failed = !done
            if (done) {
                confirming = null
                if (confirm !is RunConfirm.Cancel) watch.watchRun(confirm.run.ref, true)
                watch.refresh()
            }
        }
    }

    /** Approves, or rejects, the deployments [environments] of [run] with [comment]; true when GitHub took it. */
    suspend fun reviewDeployments(run: WorkflowRun, environments: List<Long>, approve: Boolean, comment: String): Boolean {
        val repo = GitHubRepo.parse(run.repo) ?: return false
        val done = attempt { service.reviewDeployments(repo, run.id, environments, approve, comment) } != null
        if (done) {
            if (approve) watch.watchRun(run.ref, true)
            watch.refresh()
        }
        return done
    }

    /** Opens the Run workflow form, on [repo] when one is given. */
    fun startDispatch(repo: GitHubRepo? = null, workflowPath: String? = null) {
        dispatching = RunWorkflowState(service, scope, repo ?: watch.followed.minByOrNull { it.fullName.lowercase() }, workflowPath, watch.followed)
    }

    /** Starts the workflow the form names; the new run opens and is watched. False when GitHub refused it. */
    suspend fun dispatch(form: RunWorkflowState): Boolean {
        val repo = form.repo ?: return false
        val workflow = form.workflow ?: return false
        val branch = form.branch ?: return false
        val result = attempt { service.dispatch(repo, workflow.fileName, branch.name, form.values()) } ?: return false
        dispatching = null
        result.runId?.let { id ->
            val ref = RunRef(repo.fullName, id)
            watch.watchRun(ref, true)
            openRun(ref)
        }
        watch.refresh()
        return true
    }

    /** The artifact named [name] of the earlier run [run], read from GitHub when its artifacts are not known. */
    suspend fun earlierArtifact(run: WorkflowRun, name: String): RunArtifact? {
        val known = watch.details[run.ref]?.artifacts
        val repo = GitHubRepo.parse(run.repo) ?: return null
        val artifacts = known ?: attempt { service.artifacts(repo, run.id) } ?: return null
        return artifacts.firstOrNull { it.name == name && !it.expired }
    }

    fun toggleWatch(run: WorkflowRun) {
        watch.watchRun(run.ref, run.ref !in watch.watchlist.runs)
    }

    private suspend fun rerunWhenStopped(repo: GitHubRepo, ref: RunRef, job: RunJob) {
        watch.open = watch.open + ref
        snapshotFlow { watch.run(ref)?.state }.first { it != null && it != RunState.Running && it != RunState.Queued && it != RunState.Waiting }
        attempt { service.rerunJob(repo, job.id, false) }
        watch.watchRun(ref, true)
        watch.refresh()
    }

    private suspend fun <T> attempt(block: suspend () -> T): T? = try {
        block()
    } catch (_: GitHubException) {
        null
    }
}
