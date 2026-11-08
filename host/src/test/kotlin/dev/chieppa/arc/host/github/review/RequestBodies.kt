package dev.chieppa.arc.host.github.review

import dev.chieppa.arc.host.github.api.ApiRequest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject

/** Reading the GraphQL requests a test's transport recorded. */
object RequestBodies {
    /** A GraphQL request's document and variables, with the mutation's `input` when it has one. */
    class GraphQl(val query: String, val variables: JsonObject) {
        val input: JsonObject get() = variables.getValue(GraphQlMutation.INPUT).jsonObject
    }

    fun parse(request: ApiRequest): GraphQl {
        val root = Json.parseToJsonElement(request.body!!).jsonObject
        return GraphQl((root.getValue("query") as JsonPrimitive).content, root["variables"]?.jsonObject ?: JsonObject(emptyMap()))
    }

    /** The string field [name] of [obj], or null. */
    fun string(obj: JsonObject, name: String): String? = (obj[name] as? JsonPrimitive)?.takeIf { it.isString }?.content
}
