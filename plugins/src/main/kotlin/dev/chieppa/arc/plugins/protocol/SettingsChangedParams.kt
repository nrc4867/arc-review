package dev.chieppa.arc.plugins.protocol

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/** The plugin's settings are now [values]. */
@Serializable
data class SettingsChangedParams(val values: JsonObject)
