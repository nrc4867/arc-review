package dev.chieppa.arc.host.github.api

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.intOrNull
import java.time.Instant
import java.time.format.DateTimeParseException

/** Reading fields out of GitHub's JSON, where any field may be missing or null. */
object JsonFields {
    private const val NODES = "nodes"

    fun JsonObject?.obj(name: String): JsonObject? = this?.get(name) as? JsonObject

    fun JsonObject?.array(name: String): List<JsonElement> = (this?.get(name) as? JsonArray).orEmpty()

    /** The objects in the array [name]. */
    fun JsonObject?.objects(name: String): List<JsonObject> = array(name).mapNotNull { it as? JsonObject }

    /** The `nodes` of the connection [name], as objects. */
    fun JsonObject?.nodes(name: String): List<JsonObject> = obj(name).objects(NODES)

    fun JsonObject?.string(name: String): String? = (this?.get(name) as? JsonPrimitive)?.takeIf { it.isString }?.content

    fun JsonObject?.int(name: String): Int? = (this?.get(name) as? JsonPrimitive)?.intOrNull

    fun JsonObject?.boolean(name: String): Boolean? = (this?.get(name) as? JsonPrimitive)?.booleanOrNull

    fun JsonObject?.instant(name: String): Instant? = string(name)?.let {
        try {
            Instant.parse(it)
        } catch (_: DateTimeParseException) {
            null
        }
    }
}
