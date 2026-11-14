package dev.chieppa.arc.host.github.pulls

import dev.chieppa.arc.host.github.api.JsonFields.nodes
import dev.chieppa.arc.host.github.api.JsonFields.obj
import dev.chieppa.arc.host.github.api.JsonFields.string
import kotlinx.serialization.json.JsonObject

/** The GraphQL documents that find who the person is and which teams they are in, and reading their answers. */
object ViewerQuery {
    const val IDENTITY = "query { viewer { login name avatarUrl } }"

    /** The teams [login] is in, in every organization the person can see. */
    fun teams(login: String): String =
        "query { viewer { organizations(first: 50) { nodes { login teams(first: 100, userLogins: [${SearchDocument.quote(login)}]) { nodes { slug } } } } } }"

    /** The person's login, name and picture, without teams; null when the answer names no one. */
    fun identity(data: JsonObject): Viewer? {
        val viewer = data.obj("viewer")
        val login = viewer.string("login") ?: return null
        return Viewer(login, name = viewer.string("name")?.takeIf { it.isNotBlank() }, avatarUrl = viewer.string("avatarUrl"))
    }

    fun teams(data: JsonObject): Set<ReviewRequest.Team> = data.obj("viewer").nodes("organizations").flatMap { org ->
        val name = org.string("login") ?: return@flatMap emptyList()
        org.nodes("teams").mapNotNull { team -> team.string("slug")?.let { ReviewRequest.Team(name, it) } }
    }.toSet()
}
