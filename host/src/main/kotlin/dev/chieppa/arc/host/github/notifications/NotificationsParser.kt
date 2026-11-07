package dev.chieppa.arc.host.github.notifications

import dev.chieppa.arc.host.github.api.JsonFields.boolean
import dev.chieppa.arc.host.github.api.JsonFields.instant
import dev.chieppa.arc.host.github.api.JsonFields.obj
import dev.chieppa.arc.host.github.api.JsonFields.string
import dev.chieppa.arc.host.github.repo.GitHubRepo
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

/** Reads the threads `GET /notifications` answers with; entries that do not read are left out. */
object NotificationsParser {
    fun parse(body: JsonElement): List<NotificationThread> = (body as? JsonArray).orEmpty().mapNotNull { element ->
        val thread = element as? JsonObject ?: return@mapNotNull null
        val subject = thread.obj("subject")
        NotificationThread(
            id = thread.string("id") ?: return@mapNotNull null,
            reason = thread.string("reason").orEmpty(),
            unread = thread.boolean("unread") ?: false,
            updatedAt = thread.instant("updated_at") ?: return@mapNotNull null,
            title = subject.string("title").orEmpty(),
            type = subject.string("type").orEmpty(),
            repo = thread.obj("repository").string("full_name")?.let(GitHubRepo::parse),
            number = subject.string("url")?.substringAfterLast('/')?.toIntOrNull(),
        )
    }
}
