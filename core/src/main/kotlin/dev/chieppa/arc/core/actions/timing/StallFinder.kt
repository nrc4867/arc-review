package dev.chieppa.arc.core.actions.timing

import dev.chieppa.arc.core.actions.RunJob
import dev.chieppa.arc.core.actions.RunState
import dev.chieppa.arc.core.actions.WorkflowRun

/** Finds the stalls in a running run's jobs against their [history]. */
object StallFinder {
    fun of(run: WorkflowRun, jobs: List<RunJob>, history: TimingHistory, now: Long): List<RunStall> = jobs.mapNotNull { job ->
        val key = TimingKey(run.repo, run.workflowPath, job.name)
        when (job.state) {
            RunState.Queued -> {
                val queued = job.queuedSeconds(now) ?: return@mapNotNull null
                val usual = history.queueP90(key)
                if (StallRules.queue(queued, usual)) RunStall(job.id, null, queued - (usual ?: 0)) else null
            }
            RunState.Running -> job.steps.firstOrNull { it.state == RunState.Running }?.let { step ->
                val elapsed = step.seconds(now) ?: return@let null
                val usual = history.stepP90(key, step.name)
                if (StallRules.step(elapsed, usual)) RunStall(job.id, step.number, elapsed - (usual ?: 0)) else null
            }
            else -> null
        }
    }
}
