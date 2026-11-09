package dev.chieppa.arc.core.actions.artifacts

import dev.chieppa.arc.core.actions.RunJob
import dev.chieppa.arc.core.actions.log.KeptLog

/**
 * Which job made each artifact, read from the kept logs: the job whose log names the artifact where the upload
 * action reports it. GitHub's artifact list does not say.
 */
object ArtifactJobs {
    private const val ARTIFACT = "rtifact"

    fun of(names: List<String>, jobs: List<RunJob>, logs: Map<Long, KeptLog>): Map<String, String> = names.mapNotNull { name ->
        val job = jobs.firstOrNull { job ->
            logs[job.id]?.lines?.any { it.text.contains(ARTIFACT) && mentions(it.text, name) } == true
        }
        job?.let { name to it.name }
    }.toMap()

    private fun mentions(text: String, name: String): Boolean =
        Regex("(^|[\\s'\"])${Regex.escape(name)}($|[\\s'\".,:])").containsMatchIn(text)
}
