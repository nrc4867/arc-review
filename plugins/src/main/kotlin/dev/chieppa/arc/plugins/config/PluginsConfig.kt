package dev.chieppa.arc.plugins.config

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * What the person set up for plugins: the [sources] plugins come from, the ids of those [enabled], and each plugin's
 * [settings] values by plugin id, as JSON keyed by setting.
 */
@Serializable
data class PluginsConfig(
    val sources: List<PluginSourceSpec> = emptyList(),
    val enabled: Set<String> = emptySet(),
    val settings: Map<String, JsonObject> = emptyMap(),
)
