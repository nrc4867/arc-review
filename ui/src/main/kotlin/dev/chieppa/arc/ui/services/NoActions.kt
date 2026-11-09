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
import dev.chieppa.arc.host.github.actions.Branch
import dev.chieppa.arc.host.github.actions.Conditional
import dev.chieppa.arc.host.github.actions.DispatchResult
import dev.chieppa.arc.host.github.actions.ForkChanges
import dev.chieppa.arc.host.github.actions.ShipsSince
import dev.chieppa.arc.host.github.api.GitHubException
import dev.chieppa.arc.host.github.pulls.RateBudget
import dev.chieppa.arc.host.github.repo.GitHubRepo
import java.nio.file.Path

/** No GitHub: there is no token, so every read finds nothing and every write is refused. */
object NoActions : ActionsService {
    override suspend fun hasToken(): Boolean = false
    override suspend fun runs(repo: GitHubRepo, etag: String?): Conditional<List<WorkflowRun>> = Conditional(emptyList(), null)
    override suspend fun run(repo: GitHubRepo, id: Long): WorkflowRun? = null
    override suspend fun jobs(repo: GitHubRepo, run: Long, etag: String?): Conditional<List<RunJob>> = Conditional(emptyList(), null)
    override suspend fun artifacts(repo: GitHubRepo, run: Long): List<RunArtifact> = emptyList()
    override suspend fun pendingDeployments(repo: GitHubRepo, run: Long): List<PendingDeployment> = emptyList()
    override suspend fun annotations(repo: GitHubRepo, job: RunJob): List<RunAnnotation> = emptyList()
    override suspend fun canPush(repo: GitHubRepo): Boolean = false
    override suspend fun keptLog(repo: GitHubRepo, job: RunJob): KeptLog? = null
    override suspend fun fullLog(repo: GitHubRepo, job: Long, line: (String) -> Unit): Boolean = false
    override suspend fun workflows(repo: GitHubRepo): List<Workflow> = emptyList()
    override suspend fun branches(repo: GitHubRepo): List<Branch> = emptyList()
    override suspend fun environments(repo: GitHubRepo): List<String> = emptyList()
    override suspend fun workflowSpec(repo: GitHubRepo, path: String, ref: String): WorkflowSpec? = null
    override suspend fun shipsSince(repo: GitHubRepo, environment: String, head: String): ShipsSince? = null
    override suspend fun forkChanges(repo: GitHubRepo, owner: String, branch: String): ForkChanges? = null
    override suspend fun artifactFile(run: RunRef, artifact: RunArtifact): Path? = null
    override suspend fun dispatch(repo: GitHubRepo, workflowFile: String, ref: String, inputs: Map<String, String>): DispatchResult = refuse()
    override suspend fun rerun(repo: GitHubRepo, run: Long, failedOnly: Boolean, debug: Boolean) = refuse()
    override suspend fun rerunJob(repo: GitHubRepo, job: Long, debug: Boolean) = refuse()
    override suspend fun cancel(repo: GitHubRepo, run: Long) = refuse()
    override suspend fun reviewDeployments(repo: GitHubRepo, run: Long, environments: List<Long>, approve: Boolean, comment: String) = refuse()
    override suspend fun approveFork(repo: GitHubRepo, run: Long) = refuse()
    override val budget: RateBudget? = null
    override fun cacheBytes(): Long = 0
    override val cacheCap: Long = 0

    private fun refuse(): Nothing = throw GitHubException(GitHubException.Reason.NoToken, "No GitHub token")
}
