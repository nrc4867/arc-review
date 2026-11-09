package dev.chieppa.arc.core.actions

import java.time.Instant

/**
 * One job of a run, with its steps. [labels] are the runner labels it asked for, such as `ubuntu-24.04`; [runner] the
 * runner it got, when it has one.
 */
data class RunJob(
    val id: Long,
    val runId: Long,
    val name: String,
    val status: RunStatus,
    val conclusion: RunConclusion?,
    val createdAt: Instant?,
    val startedAt: Instant?,
    val completedAt: Instant?,
    val steps: List<RunStep> = emptyList(),
    val labels: List<String> = emptyList(),
    val runner: String? = null,
) {
    val state: RunState get() = RunState.of(status, conclusion)

    /** How long it has run, or ran, by [now] in epoch seconds; null before it starts. */
    fun seconds(now: Long): Long? {
        val start = startedAt ?: return null
        val end = completedAt?.epochSecond ?: now
        return (end - start.epochSecond).coerceAtLeast(0)
    }

    /** How long it waited for a runner, or has waited by [now]; null when it was never queued. */
    fun queuedSeconds(now: Long): Long? {
        val created = createdAt ?: return null
        val end = startedAt?.epochSecond ?: now
        return (end - created.epochSecond).coerceAtLeast(0)
    }
}
