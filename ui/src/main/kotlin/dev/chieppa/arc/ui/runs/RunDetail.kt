package dev.chieppa.arc.ui.runs

import dev.chieppa.arc.core.actions.RunAnnotation
import dev.chieppa.arc.core.actions.RunArtifact
import dev.chieppa.arc.core.actions.RunJob
import dev.chieppa.arc.core.actions.RunRecord
import dev.chieppa.arc.core.actions.WorkflowRun
import dev.chieppa.arc.core.actions.WorkflowSpec
import dev.chieppa.arc.core.actions.log.KeptLog

/**
 * What is known of one run beyond its row: its jobs, the kept logs of finished ones by job id, annotations, its
 * artifacts once it finished (null before they are read), its workflow file and the etag its jobs were read at.
 */
data class RunDetail(
    val run: WorkflowRun,
    val jobs: List<RunJob> = emptyList(),
    val logs: Map<Long, KeptLog> = emptyMap(),
    val annotations: List<RunAnnotation> = emptyList(),
    val artifacts: List<RunArtifact>? = null,
    val spec: WorkflowSpec? = null,
    val jobsEtag: String? = null,
    val readAt: Long = 0,
) {
    fun record(seenAt: Long): RunRecord = RunRecord(run, jobs, logs, annotations, artifacts.orEmpty(), seenAt)

    companion object {
        fun of(record: RunRecord): RunDetail = RunDetail(record.run, record.jobs, record.logs, record.annotations, record.artifacts)
    }
}
