package dev.chieppa.arc.core.actions

import java.time.Instant

/** Runs, jobs and steps for tests, at fixed times. */
object RunSamples {
    val T0: Instant = Instant.parse("2026-11-09T09:00:00Z")

    fun run(
        id: Long = 1,
        repo: String = "lumen/api",
        status: RunStatus = RunStatus.Completed,
        conclusion: RunConclusion? = RunConclusion.Success,
        actor: String? = "jpark",
        workflowPath: String = ".github/workflows/ci.yml",
        workflowName: String = "CI",
        headSha: String = "9c1e2fa",
        pulls: List<Int> = emptyList(),
        headOwner: String? = null,
        branch: String? = "main",
        event: String = "push",
        updatedAt: Instant = T0,
    ) = WorkflowRun(
        repo = repo, id = id, workflowId = 7, workflowName = workflowName, workflowPath = workflowPath, title = "title $id",
        number = id.toInt(), attempt = 1, event = event, status = status, conclusion = conclusion, headBranch = branch,
        headSha = headSha, actor = actor, triggeringActor = actor, createdAt = T0, startedAt = T0, updatedAt = updatedAt,
        pulls = pulls, headOwner = headOwner,
    )

    fun step(number: Int, name: String, startSecond: Long, endSecond: Long?, conclusion: RunConclusion? = RunConclusion.Success) = RunStep(
        number, name,
        if (endSecond == null) RunStatus.InProgress else RunStatus.Completed,
        if (endSecond == null) null else conclusion,
        T0.plusSeconds(startSecond),
        endSecond?.let { T0.plusSeconds(it) },
    )

    fun job(id: Long, name: String, steps: List<RunStep>, conclusion: RunConclusion? = RunConclusion.Success, queued: Long = 5) = RunJob(
        id = id, runId = 1, name = name,
        status = if (conclusion == null) RunStatus.InProgress else RunStatus.Completed,
        conclusion = conclusion, createdAt = T0.minusSeconds(queued), startedAt = T0,
        completedAt = if (conclusion == null) null else steps.mapNotNull { it.completedAt }.maxOrNull(), steps = steps,
    )
}
