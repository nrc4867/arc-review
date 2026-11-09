package dev.chieppa.arc.core.actions

import java.time.Instant

/** One step of a job: its [number] from 1, name, and when it started and finished. */
data class RunStep(
    val number: Int,
    val name: String,
    val status: RunStatus,
    val conclusion: RunConclusion?,
    val startedAt: Instant?,
    val completedAt: Instant?,
) {
    val state: RunState get() = RunState.of(status, conclusion)

    /** How long it has run, or ran, by [now] in epoch seconds; null before it starts. */
    fun seconds(now: Long): Long? {
        val start = startedAt ?: return null
        val end = completedAt?.epochSecond ?: now
        return (end - start.epochSecond).coerceAtLeast(0)
    }
}
