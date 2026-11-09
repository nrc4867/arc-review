package dev.chieppa.arc.store.actions

import dev.chieppa.arc.core.actions.RunAnnotation
import dev.chieppa.arc.core.actions.RunArtifact
import dev.chieppa.arc.core.actions.RunConclusion
import dev.chieppa.arc.core.actions.RunJob
import dev.chieppa.arc.core.actions.RunRecord
import dev.chieppa.arc.core.actions.RunStatus
import dev.chieppa.arc.core.actions.RunStep
import dev.chieppa.arc.core.actions.WorkflowRun
import dev.chieppa.arc.core.actions.log.KeptLine
import dev.chieppa.arc.core.actions.log.KeptLog
import dev.chieppa.arc.core.actions.log.LogSilence
import java.time.Instant
import java.time.format.DateTimeParseException

/** Turns a kept run into what is written and back; a record that does not read is null. */
internal object RunRecordMapping {
    fun toStored(record: RunRecord): StoredRunRecord = StoredRunRecord(
        run = run(record.run),
        jobs = record.jobs.map(::job),
        logs = record.logs.mapValues { (_, log) ->
            StoredLog(log.lines.map { StoredLine(it.index, it.step, it.epochSecond, it.text) }, log.totalLines, log.totalBytes, log.silences.map { listOf(it.afterIndex.toLong(), it.seconds) })
        },
        annotations = record.annotations.map { StoredAnnotation(it.path, it.line, it.level, it.message, it.job, it.title) },
        artifacts = record.artifacts.map { StoredArtifact(it.id, it.name, it.sizeBytes, it.expiresAt?.toString(), it.expired, it.createdAt?.toString()) },
        seenAt = record.seenAt,
    )

    fun toRecord(stored: StoredRunRecord): RunRecord? {
        val run = run(stored.run) ?: return null
        return RunRecord(
            run = run,
            jobs = stored.jobs.map(::job),
            logs = stored.logs.mapValues { (_, log) ->
                KeptLog(
                    log.lines.map { KeptLine(it.i, it.s, it.t, it.x) },
                    log.totalLines,
                    log.totalBytes,
                    log.silences.filter { it.size == 2 }.map { LogSilence(it[0].toInt(), it[1]) },
                )
            },
            annotations = stored.annotations.map { RunAnnotation(it.path, it.line, it.level, it.message, it.job, it.title) },
            artifacts = stored.artifacts.map { RunArtifact(it.id, it.name, it.size, instant(it.expiresAt), it.expired, instant(it.createdAt)) },
            seenAt = stored.seenAt,
        )
    }

    private fun run(run: WorkflowRun) = StoredRun(
        run.repo, run.id, run.workflowId, run.workflowName, run.workflowPath, run.title, run.number, run.attempt, run.event,
        run.status.name, run.conclusion?.name, run.headBranch, run.headSha, run.actor, run.triggeringActor, run.createdAt.toString(),
        run.startedAt?.toString(), run.updatedAt.toString(), run.pulls, run.headOwner, run.htmlUrl,
    )

    private fun run(stored: StoredRun): WorkflowRun? {
        val created = instant(stored.createdAt) ?: return null
        return WorkflowRun(
            stored.repo, stored.id, stored.workflowId, stored.workflowName, stored.workflowPath, stored.title, stored.number, stored.attempt,
            stored.event, status(stored.status), conclusion(stored.conclusion), stored.headBranch, stored.headSha, stored.actor,
            stored.triggeringActor, created, instant(stored.startedAt), instant(stored.updatedAt) ?: created, stored.pulls, stored.headOwner, stored.htmlUrl,
        )
    }

    private fun job(job: RunJob) = StoredJob(
        job.id, job.runId, job.name, job.status.name, job.conclusion?.name, job.createdAt?.toString(), job.startedAt?.toString(),
        job.completedAt?.toString(), job.steps.map { StoredStep(it.number, it.name, it.status.name, it.conclusion?.name, it.startedAt?.toString(), it.completedAt?.toString()) },
        job.labels, job.runner,
    )

    private fun job(job: StoredJob) = RunJob(
        job.id, job.runId, job.name, status(job.status), conclusion(job.conclusion), instant(job.createdAt), instant(job.startedAt), instant(job.completedAt),
        job.steps.map { RunStep(it.number, it.name, status(it.status), conclusion(it.conclusion), instant(it.startedAt), instant(it.completedAt)) },
        job.labels, job.runner,
    )

    private fun status(name: String): RunStatus = RunStatus.entries.firstOrNull { it.name == name } ?: RunStatus.Completed

    private fun conclusion(name: String?): RunConclusion? = RunConclusion.entries.firstOrNull { it.name == name }

    private fun instant(text: String?): Instant? = text?.let {
        try {
            Instant.parse(it)
        } catch (_: DateTimeParseException) {
            null
        }
    }
}
