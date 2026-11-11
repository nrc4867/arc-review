package dev.chieppa.arc.plugins.config

import dev.chieppa.arc.plugins.manifest.Capabilities
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * What the person set up for plugins: the [sources] plugins come from, the ids of those [enabled], the capabilities
 * the person [approved] for each when enabling it, each plugin's [settings] values by plugin id, as JSON keyed by
 * setting, and the keys of its secret settings that hold a value, in [secrets]: the values themselves are kept in the
 * system's keychain. An enabled plugin whose manifest asks for more than was approved does not run until the person enables it
 * again.
 */
@Serializable
data class PluginsConfig(
    val sources: List<PluginSourceSpec> = emptyList(),
    val enabled: Set<String> = emptySet(),
    val approved: Map<String, Capabilities> = emptyMap(),
    val settings: Map<String, JsonObject> = emptyMap(),
    val secrets: Map<String, Set<String>> = emptyMap(),
)
