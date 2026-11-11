package dev.chieppa.arc.plugins.manifest

import kotlinx.serialization.Serializable

/**
 * A plugin's `plugin.json`: its [id], the [name] shown (the id when absent), its [version], the plugin API version it
 * was written for, the TypeScript or JavaScript [entry] file relative to its folder, an optional [icon] name and what
 * it may do, its [capabilities], which Arc shows before any of its code runs.
 */
@Serializable
data class PluginManifest(
    val id: String,
    val version: String,
    val apiVersion: Int,
    val entry: String,
    val name: String? = null,
    val description: String? = null,
    val icon: String? = null,
    val capabilities: Capabilities = Capabilities(),
) {
    /** The name to show: [name], or the id when the manifest gives none. */
    val title: String get() = name ?: id
}
