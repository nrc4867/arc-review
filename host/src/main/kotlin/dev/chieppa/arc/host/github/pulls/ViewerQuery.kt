package dev.chieppa.arc.host.github.pulls

import dev.chieppa.arc.host.github.api.JsonFields.nodes
import dev.chieppa.arc.host.github.api.JsonFields.obj
import dev.chieppa.arc.host.github.api.JsonFields.string
import kotlinx.serialization.json.JsonObject

/** The GraphQL documents that find who the person is and which teams they are in, and reading their answers. */
object ViewerQuery {
    const val LOGIN = "query { viewer { login } }"

    /** The teams [login] is in, in every organization the person can see. */
    fun teams(login: String): String =
        "query { viewer { organizations(first: 50) { nodes { login teams(first: 100, userLogins: [${SearchDocument.quote(login)}]) { nodes { slug } } } } } }"

    fun login(data: JsonObject): String? = data.obj("viewer").string("login")

    fun teams(data: JsonObject): Set<ReviewRequest.Team> = data.obj("viewer").nodes("organizations").flatMap { org ->
        val name = org.string("login") ?: return@flatMap emptyList()
        org.nodes("teams").mapNotNull { team -> team.string("slug")?.let { ReviewRequest.Team(name, it) } }
    }.toSet()
}
