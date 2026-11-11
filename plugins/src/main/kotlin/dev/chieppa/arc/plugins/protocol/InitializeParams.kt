package dev.chieppa.arc.plugins.protocol

import dev.chieppa.arc.plugins.manifest.Capabilities
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * The first request to a plugin's worker: the API version Arc speaks, which [plugin] it is, its [entry] file to load
 * (TypeScript or JavaScript, an absolute path), the plugin's stored [settings] values and its declared [capabilities].
 */
@Serializable
data class InitializeParams(
    val apiVersion: Int,
    val plugin: PluginInfo,
    val entry: String,
    val settings: JsonObject = JsonObject(emptyMap()),
    val capabilities: Capabilities = Capabilities(),
)
