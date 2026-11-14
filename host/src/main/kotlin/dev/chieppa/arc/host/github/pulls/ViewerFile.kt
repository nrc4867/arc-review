package dev.chieppa.arc.host.github.pulls

import dev.chieppa.arc.host.github.api.JsonFields.instant
import dev.chieppa.arc.host.github.api.JsonFields.obj
import dev.chieppa.arc.host.github.api.JsonFields.objects
import dev.chieppa.arc.host.github.api.JsonFields.string
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

/** A [ViewerRecord] kept as JSON at [path]: nothing is read from a missing or unreadable file, and a failed write is dropped. */
class ViewerFile(private val path: Path) {
    fun read(): ViewerRecord? = try {
        if (Files.isRegularFile(path)) parse(Json.parseToJsonElement(Files.readString(path)) as? JsonObject) else null
    } catch (_: IOException) {
        null
    } catch (_: SerializationException) {
        null
    }

    fun write(record: ViewerRecord) {
        try {
            path.parent?.let(Files::createDirectories)
            Files.writeString(path, json(record).toString())
        } catch (_: IOException) {
            // The record stays in memory; the next run asks GitHub again.
        }
    }

    private fun parse(json: JsonObject?): ViewerRecord? {
        val key = json.string(KEY) ?: return null
        val at = json.instant(AT) ?: return null
        val viewer = json.obj(VIEWER)
        val login = viewer.string(LOGIN) ?: return null
        val teams = viewer.objects(TEAMS).mapNotNull { team ->
            val organization = team.string(ORGANIZATION) ?: return@mapNotNull null
            team.string(SLUG)?.let { ReviewRequest.Team(organization, it) }
        }.toSet()
        return ViewerRecord(key, Viewer(login, teams, viewer.string(NAME), viewer.string(AVATAR)), at)
    }

    private fun json(record: ViewerRecord): JsonObject = buildJsonObject {
        put(KEY, record.key)
        put(AT, record.at.toString())
        putJsonObject(VIEWER) {
            put(LOGIN, record.viewer.login)
            record.viewer.name?.let { put(NAME, it) }
            record.viewer.avatarUrl?.let { put(AVATAR, it) }
            putJsonArray(TEAMS) {
                record.viewer.teams.forEach { team -> addJsonObject { put(ORGANIZATION, team.organization); put(SLUG, team.slug) } }
            }
        }
    }

    private companion object {
        const val KEY = "token"
        const val AT = "at"
        const val VIEWER = "viewer"
        const val LOGIN = "login"
        const val NAME = "name"
        const val AVATAR = "avatarUrl"
        const val TEAMS = "teams"
        const val ORGANIZATION = "organization"
        const val SLUG = "slug"
    }
}
