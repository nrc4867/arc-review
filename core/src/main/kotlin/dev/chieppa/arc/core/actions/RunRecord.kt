package dev.chieppa.arc.core.actions

import dev.chieppa.arc.core.actions.log.KeptLog

/**
 * What the app keeps of a run: the run and its jobs as last read, the kept part of each finished job's log by job id,
 * its annotations and artifacts, and when it was last in any filter, in epoch milliseconds.
 */
data class RunRecord(
    val run: WorkflowRun,
    val jobs: List<RunJob> = emptyList(),
    val logs: Map<Long, KeptLog> = emptyMap(),
    val annotations: List<RunAnnotation> = emptyList(),
    val artifacts: List<RunArtifact> = emptyList(),
    val seenAt: Long = 0,
)
