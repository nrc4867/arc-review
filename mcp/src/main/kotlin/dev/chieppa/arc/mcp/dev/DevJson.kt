package dev.chieppa.arc.mcp.dev

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive

/** The development tools' answers as JSON text. */
internal object DevJson {
    private val json = Json {
        prettyPrint = true
        explicitNulls = false
        encodeDefaults = true
    }

    fun <T> encode(serializer: KSerializer<T>, value: T): String = json.encodeToString(serializer, value)

    /** [state]'s name as the answers write it. */
    fun name(state: DevPluginState): String = (json.encodeToJsonElement(DevPluginState.serializer(), state) as JsonPrimitive).content
}
