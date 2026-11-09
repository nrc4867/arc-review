package dev.chieppa.arc.ui.services

import dev.chieppa.arc.core.actions.PendingDeployment
import dev.chieppa.arc.core.actions.RunAnnotation
import dev.chieppa.arc.core.actions.RunArtifact
import dev.chieppa.arc.core.actions.RunJob
import dev.chieppa.arc.core.actions.RunRef
import dev.chieppa.arc.core.actions.Workflow
import dev.chieppa.arc.core.actions.WorkflowRun
import dev.chieppa.arc.core.actions.WorkflowSpec
import dev.chieppa.arc.core.actions.log.KeptLog
import dev.chieppa.arc.core.actions.log.LogFilter
import dev.chieppa.arc.host.github.GitHub
import dev.chieppa.arc.host.github.actions.Branch
import dev.chieppa.arc.host.github.actions.Conditional
import dev.chieppa.arc.host.github.actions.DispatchResult
import dev.chieppa.arc.host.github.actions.ForkChanges
import dev.chieppa.arc.host.github.actions.ShipsSince
import dev.chieppa.arc.host.github.actions.WorkflowFile
import dev.chieppa.arc.host.github.pulls.RateBudget
import dev.chieppa.arc.host.github.repo.GitHubRepo
import dev.chieppa.arc.store.actions.ArtifactCache
import dev.chieppa.arc.ui.util.ArcDispatchers
import kotlinx.coroutines.withContext
import java.nio.file.Path

/**
 * [ActionsService] on [github]. Workflow files are read through [clonedFile] from the app's clones where they hold
 * them, else asked of GitHub; artifacts are kept in [cache]. Blocking work runs on [dispatchers]' io.
 */
class GitHubActions(
    private val github: GitHub,
    private val cache: ArtifactCache,
    private val clonedFile: suspend (GitHubRepo, String, String) -> String? = { _, _, _ -> null },
    private val dispatchers: ArcDispatchers = ArcDispatchers.Real,
) : ActionsService {
    private val actions get() = github.actions
    private val writer get() = github.actionsWriter

    override suspend fun hasToken(): Boolean = withContext(dispatchers.io) { github.hasToken() }
    override suspend fun runs(repo: GitHubRepo, etag: String?): Conditional<List<WorkflowRun>> = actions.runs(repo, etag)
    override suspend fun run(repo: GitHubRepo, id: Long): WorkflowRun? = actions.run(repo, id)
    override suspend fun jobs(repo: GitHubRepo, run: Long, etag: String?): Conditional<List<RunJob>> = actions.jobs(repo, run, etag)
    override suspend fun artifacts(repo: GitHubRepo, run: Long): List<RunArtifact> = actions.artifacts(repo, run)
    override suspend fun pendingDeployments(repo: GitHubRepo, run: Long): List<PendingDeployment> = actions.pendingDeployments(repo, run)
    override suspend fun annotations(repo: GitHubRepo, job: RunJob): List<RunAnnotation> = actions.annotations(repo, job)
    override suspend fun canPush(repo: GitHubRepo): Boolean = actions.canPush(repo)

    override suspend fun keptLog(repo: GitHubRepo, job: RunJob): KeptLog? = actions.jobLog(repo, job.id) { input ->
        val filter = LogFilter(job.steps)
        input.bufferedReader().useLines { lines -> lines.forEach(filter::accept) }
        filter.finish()
    }

    override suspend fun fullLog(repo: GitHubRepo, job: Long, line: (String) -> Unit): Boolean =
        actions.jobLog(repo, job) { input -> input.bufferedReader().useLines { lines -> lines.forEach(line) } } != null

    override suspend fun workflows(repo: GitHubRepo): List<Workflow> = actions.workflows(repo)
    override suspend fun branches(repo: GitHubRepo): List<Branch> = actions.branches(repo)
    override suspend fun environments(repo: GitHubRepo): List<String> = actions.environments(repo)

    override suspend fun workflowSpec(repo: GitHubRepo, path: String, ref: String): WorkflowSpec? {
        val text = clonedFile(repo, path, ref) ?: actions.file(repo, path, ref) ?: return null
        return withContext(dispatchers.default) { WorkflowFile.parse(text) }
    }

    override suspend fun shipsSince(repo: GitHubRepo, environment: String, head: String): ShipsSince? = actions.shipsSince(repo, environment, head)
    override suspend fun forkChanges(repo: GitHubRepo, owner: String, branch: String): ForkChanges? = actions.forkChanges(repo, owner, branch)

    override suspend fun artifactFile(run: RunRef, artifact: RunArtifact): Path? {
        withContext(dispatchers.io) { cache.get(run, artifact.id) }?.let { return it }
        if (artifact.expired) return null
        val repo = GitHubRepo.parse(run.repo) ?: return null
        return actions.artifact(repo, artifact.id) { input -> cache.put(run, artifact.id, input) }
    }

    override suspend fun dispatch(repo: GitHubRepo, workflowFile: String, ref: String, inputs: Map<String, String>): DispatchResult =
        writer.dispatch(repo, workflowFile, ref, inputs)

    override suspend fun rerun(repo: GitHubRepo, run: Long, failedOnly: Boolean, debug: Boolean) {
        if (failedOnly) writer.rerunFailed(repo, run, debug) else writer.rerun(repo, run, debug)
    }

    override suspend fun rerunJob(repo: GitHubRepo, job: Long, debug: Boolean) = writer.rerunJob(repo, job, debug)
    override suspend fun cancel(repo: GitHubRepo, run: Long) = writer.cancel(repo, run)

    override suspend fun reviewDeployments(repo: GitHubRepo, run: Long, environments: List<Long>, approve: Boolean, comment: String) =
        writer.reviewDeployments(repo, run, environments, approve, comment)

    override suspend fun approveFork(repo: GitHubRepo, run: Long) = writer.approveFork(repo, run)

    override val budget: RateBudget? get() = github.restBudget

    override fun cacheBytes(): Long = cache.size()

    override val cacheCap: Long get() = cache.capBytes
}
