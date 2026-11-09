package dev.chieppa.arc.host.github.actions

import dev.chieppa.arc.core.actions.RunAnnotation
import dev.chieppa.arc.host.github.api.JsonFields.int
import dev.chieppa.arc.host.github.api.JsonFields.string
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

/** Reads a job's annotations from GitHub's check run answer, naming them after [job]. */
object AnnotationParser {
    fun list(body: JsonElement, job: String): List<RunAnnotation> = (body as? JsonArray).orEmpty().mapNotNull { item ->
        val json = item as? JsonObject ?: return@mapNotNull null
        RunAnnotation(
            path = json.string("path").orEmpty(),
            line = json.int("start_line") ?: 0,
            level = json.string("annotation_level").orEmpty(),
            message = json.string("message").orEmpty(),
            job = job,
            title = json.string("title"),
        )
    }
}
