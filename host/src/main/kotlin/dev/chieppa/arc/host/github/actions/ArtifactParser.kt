package dev.chieppa.arc.host.github.actions

import dev.chieppa.arc.core.actions.RunArtifact
import dev.chieppa.arc.host.github.actions.ActionsJson.long
import dev.chieppa.arc.host.github.api.JsonFields.boolean
import dev.chieppa.arc.host.github.api.JsonFields.instant
import dev.chieppa.arc.host.github.api.JsonFields.objects
import dev.chieppa.arc.host.github.api.JsonFields.string
import kotlinx.serialization.json.JsonObject

/** Reads a run's artifacts from GitHub's REST answer. */
object ArtifactParser {
    fun list(body: JsonObject): List<RunArtifact> = body.objects("artifacts").mapNotNull { json ->
        RunArtifact(
            id = json.long("id") ?: return@mapNotNull null,
            name = json.string("name").orEmpty(),
            sizeBytes = json.long("size_in_bytes") ?: 0,
            expiresAt = json.instant("expires_at"),
            expired = json.boolean("expired") ?: false,
            createdAt = json.instant("created_at"),
        )
    }
}
