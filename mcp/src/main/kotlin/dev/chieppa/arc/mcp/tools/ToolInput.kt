package dev.chieppa.arc.mcp.tools

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray

/** A tool's arguments as a JSON schema of an object: its [properties] by name, and those [required]. */
data class ToolInput(val properties: JsonObject = JsonObject(emptyMap()), val required: List<String> = emptyList()) {
    /** Builds a [ToolInput] one property at a time. */
    class Builder {
        private val properties = mutableMapOf<String, JsonObject>()
        private val required = mutableListOf<String>()

        fun string(name: String, doc: String, required: Boolean = false, oneOf: List<String> = emptyList()) = add(name, required) {
            put(TYPE, STRING)
            put(DESCRIPTION, doc)
            if (oneOf.isNotEmpty()) putJsonArray(ENUM) { oneOf.forEach { add(JsonPrimitive(it)) } }
        }

        fun integer(name: String, doc: String, minimum: Int, maximum: Int, required: Boolean = false) = add(name, required) {
            put(TYPE, INTEGER)
            put(DESCRIPTION, doc)
            put(MINIMUM, minimum)
            put(MAXIMUM, maximum)
        }

        fun boolean(name: String, doc: String) = add(name, false) {
            put(TYPE, BOOLEAN)
            put(DESCRIPTION, doc)
        }

        fun obj(name: String, doc: String) = add(name, false) {
            put(TYPE, OBJECT)
            put(DESCRIPTION, doc)
        }

        private fun add(name: String, isRequired: Boolean, schema: JsonObjectBuilder.() -> Unit) {
            properties[name] = buildJsonObject(schema)
            if (isRequired) required += name
        }

        fun build(): ToolInput = ToolInput(JsonObject(properties), required.toList())
    }

    companion object {
        private const val TYPE = "type"
        private const val DESCRIPTION = "description"
        private const val ENUM = "enum"
        private const val MINIMUM = "minimum"
        private const val MAXIMUM = "maximum"
        private const val STRING = "string"
        private const val INTEGER = "integer"
        private const val BOOLEAN = "boolean"
        private const val OBJECT = "object"

        fun of(build: Builder.() -> Unit): ToolInput = Builder().apply(build).build()
    }
}
