package dev.chieppa.arc.plugins.protocol

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/** Set the plugin's setting [key] to [value]. */
@Serializable
data class SettingsSetParams(val key: String, val value: JsonElement)
