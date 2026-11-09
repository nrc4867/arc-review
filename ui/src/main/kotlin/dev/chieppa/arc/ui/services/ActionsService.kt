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
import dev.chieppa.arc.host.github.pulls.RateBudget
import dev.chieppa.arc.host.github.repo.GitHubRepo
import java.nio.file.Path

/**
 * What the window asks of GitHub Actions: reads, polled or asked once, and the writes a person confirms. Every call
 * throws `GitHubException` when GitHub gives nothing to use. Nothing here is offered to agents.
 */
interface ActionsService {
    suspend fun hasToken(): Boolean
    suspend fun runs(repo: GitHubRepo, etag: String?): Conditional<List<WorkflowRun>>
    suspend fun run(repo: GitHubRepo, id: Long): WorkflowRun?
    suspend fun jobs(repo: GitHubRepo, run: Long, etag: String?): Conditional<List<RunJob>>
    suspend fun artifacts(repo: GitHubRepo, run: Long): List<RunArtifact>
    suspend fun pendingDeployments(repo: GitHubRepo, run: Long): List<PendingDeployment>
    suspend fun annotations(repo: GitHubRepo, job: RunJob): List<RunAnnotation>
    suspend fun canPush(repo: GitHubRepo): Boolean

    /** What is kept of finished [job]'s log, read once through the log filter; null when GitHub has no log. */
    suspend fun keptLog(repo: GitHubRepo, job: RunJob): KeptLog?

    /** Every line of [job]'s log to [line] as it arrives; false when GitHub has no log any more. */
    suspend fun fullLog(repo: GitHubRepo, job: Long, line: (String) -> Unit): Boolean

    suspend fun workflows(repo: GitHubRepo): List<Workflow>
    suspend fun branches(repo: GitHubRepo): List<Branch>
    suspend fun environments(repo: GitHubRepo): List<String>

    /** The workflow file [path] of [repo] at [ref], read from the app's clone where it has it, else from GitHub. */
    suspend fun workflowSpec(repo: GitHubRepo, path: String, ref: String): WorkflowSpec?

    suspend fun shipsSince(repo: GitHubRepo, environment: String, head: String): ShipsSince?
    suspend fun forkChanges(repo: GitHubRepo, owner: String, branch: String): ForkChanges?

    /** The file of [artifact] of [run], from the cache or downloaded into it; null when it has expired. */
    suspend fun artifactFile(run: RunRef, artifact: RunArtifact): Path?

    suspend fun dispatch(repo: GitHubRepo, workflowFile: String, ref: String, inputs: Map<String, String>): DispatchResult
    suspend fun rerun(repo: GitHubRepo, run: Long, failedOnly: Boolean, debug: Boolean)
    suspend fun rerunJob(repo: GitHubRepo, job: Long, debug: Boolean)
    suspend fun cancel(repo: GitHubRepo, run: Long)
    suspend fun reviewDeployments(repo: GitHubRepo, run: Long, environments: List<Long>, approve: Boolean, comment: String)
    suspend fun approveFork(repo: GitHubRepo, run: Long)

    /** The REST budget GitHub reported last. */
    val budget: RateBudget?

    /** How many bytes the artifact cache holds, and at most. */
    fun cacheBytes(): Long

    val cacheCap: Long
}
