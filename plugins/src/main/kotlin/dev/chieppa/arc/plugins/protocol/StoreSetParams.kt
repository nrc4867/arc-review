package dev.chieppa.arc.plugins.protocol

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/** Keep [value] under [key] in the plugin's private store. */
@Serializable
data class StoreSetParams(val key: String, val value: JsonElement)
