package dev.chieppa.arc.core.actions.timing

import dev.chieppa.arc.core.actions.RunJob
import dev.chieppa.arc.core.actions.RunState

/**
 * The timings of the last [LIMIT] successful runs of each job, a few numbers each, which a running step's time is
 * held against. Every change returns a new history.
 */
data class TimingHistory(val jobs: Map<TimingKey, List<TimingSample>> = emptyMap()) {
    /** The usual 90th percentile of step [step] of [key], or null with no history. */
    fun stepP90(key: TimingKey, step: String): Long? = Percentiles.of(jobs[key].orEmpty().mapNotNull { it.steps[step] })

    /** The usual 90th percentile of the whole of [key]: the sum of its steps' times in each sample. */
    fun jobP90(key: TimingKey): Long? = Percentiles.of(jobs[key].orEmpty().map { it.steps.values.sum() })

    /** The usual 90th percentile of how long [key] waits for a runner. */
    fun queueP90(key: TimingKey): Long? = Percentiles.of(jobs[key].orEmpty().map { it.queuedSeconds })

    /** The history with [job] of run [runId] recorded under [key] when it succeeded and is not recorded yet. */
    fun record(key: TimingKey, runId: Long, job: RunJob): TimingHistory {
        if (job.state != RunState.Succeeded) return this
        val samples = jobs[key].orEmpty()
        if (samples.any { it.runId == runId }) return this
        val steps = job.steps.mapNotNull { step ->
            val start = step.startedAt ?: return@mapNotNull null
            val end = step.completedAt ?: return@mapNotNull null
            step.name to (end.epochSecond - start.epochSecond).coerceAtLeast(0)
        }.toMap()
        val queued = job.queuedSeconds(job.startedAt?.epochSecond ?: 0) ?: 0
        return copy(jobs = jobs + (key to (samples + TimingSample(runId, queued, steps)).takeLast(LIMIT)))
    }

    companion object {
        const val LIMIT = 20
    }
}
