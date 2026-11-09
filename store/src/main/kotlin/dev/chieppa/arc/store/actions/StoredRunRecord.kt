package dev.chieppa.arc.store.actions

import kotlinx.serialization.Serializable

/** A kept run as written: the run, its jobs, the kept log lines by job id, annotations and artifacts. */
@Serializable
internal data class StoredRunRecord(
    val run: StoredRun,
    val jobs: List<StoredJob> = emptyList(),
    val logs: Map<Long, StoredLog> = emptyMap(),
    val annotations: List<StoredAnnotation> = emptyList(),
    val artifacts: List<StoredArtifact> = emptyList(),
    val seenAt: Long = 0,
)

@Serializable
internal data class StoredRun(
    val repo: String,
    val id: Long,
    val workflowId: Long,
    val workflowName: String,
    val workflowPath: String,
    val title: String,
    val number: Int,
    val attempt: Int,
    val event: String,
    val status: String,
    val conclusion: String? = null,
    val headBranch: String? = null,
    val headSha: String,
    val actor: String? = null,
    val triggeringActor: String? = null,
    val createdAt: String,
    val startedAt: String? = null,
    val updatedAt: String,
    val pulls: List<Int> = emptyList(),
    val headOwner: String? = null,
    val htmlUrl: String = "",
)

@Serializable
internal data class StoredJob(
    val id: Long,
    val runId: Long,
    val name: String,
    val status: String,
    val conclusion: String? = null,
    val createdAt: String? = null,
    val startedAt: String? = null,
    val completedAt: String? = null,
    val steps: List<StoredStep> = emptyList(),
    val labels: List<String> = emptyList(),
    val runner: String? = null,
)

@Serializable
internal data class StoredStep(
    val number: Int,
    val name: String,
    val status: String,
    val conclusion: String? = null,
    val startedAt: String? = null,
    val completedAt: String? = null,
)

@Serializable
internal data class StoredLog(
    val lines: List<StoredLine> = emptyList(),
    val totalLines: Int = 0,
    val totalBytes: Long = 0,
    val silences: List<List<Long>> = emptyList(),
)

@Serializable
internal data class StoredLine(val i: Int, val s: Int, val t: Long? = null, val x: String)

@Serializable
internal data class StoredAnnotation(val path: String, val line: Int, val level: String, val message: String, val job: String, val title: String? = null)

@Serializable
internal data class StoredArtifact(val id: Long, val name: String, val size: Long, val expiresAt: String? = null, val expired: Boolean = false, val createdAt: String? = null)
