package dev.chieppa.arc.core.actions

import java.time.Instant

/**
 * One run of a workflow as GitHub lists it. [repo] is `owner/name`; [workflowPath] the workflow's file, such as
 * `.github/workflows/ci.yml`; [title] what GitHub shows for the run, such as a commit's or pull request's title;
 * [actor] who the run is for and [triggeringActor] who started this attempt; [pulls] the numbers of the pull requests
 * in the same repo it ran for; [headOwner] the owner of the repo its commit came from, another one for a fork.
 */
data class WorkflowRun(
    val repo: String,
    val id: Long,
    val workflowId: Long,
    val workflowName: String,
    val workflowPath: String,
    val title: String,
    val number: Int,
    val attempt: Int,
    val event: String,
    val status: RunStatus,
    val conclusion: RunConclusion?,
    val headBranch: String?,
    val headSha: String,
    val actor: String?,
    val triggeringActor: String?,
    val createdAt: Instant,
    val startedAt: Instant?,
    val updatedAt: Instant,
    val pulls: List<Int> = emptyList(),
    val headOwner: String? = null,
    val htmlUrl: String = "",
) {
    val ref: RunRef get() = RunRef(repo, id)
    val state: RunState get() = RunState.of(status, conclusion)

    /** The workflow's file name, such as `ci.yml`. */
    val workflowFile: String get() = workflowPath.substringAfterLast('/')

    /** Whether its commit came from a fork of [repo]. */
    val fromFork: Boolean get() = headOwner != null && !headOwner.equals(repo.substringBefore('/'), ignoreCase = true)

    /** Whether it waits for a maintainer to approve running it, as a first-time contributor's fork run does. */
    val awaitsForkApproval: Boolean get() = conclusion == RunConclusion.ActionRequired && fromFork

    /** How long it has run, or ran, by [now] in epoch seconds; null before it starts. */
    fun seconds(now: Long): Long? {
        val start = startedAt ?: return null
        val end = if (state.finished) updatedAt.epochSecond else now
        return (end - start.epochSecond).coerceAtLeast(0)
    }
}
