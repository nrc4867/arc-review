package dev.chieppa.arc.core.actions.timing

/** How long one successful run of a job took: how long it waited for a runner and each step's seconds by name. */
data class TimingSample(val runId: Long, val queuedSeconds: Long, val steps: Map<String, Long>)
