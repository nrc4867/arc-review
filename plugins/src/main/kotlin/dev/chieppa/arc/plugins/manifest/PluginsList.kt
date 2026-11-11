package dev.chieppa.arc.plugins.manifest

import kotlinx.serialization.Serializable

/** A `plugins.json`: the [plugins] a folder holds, each a folder relative to it with its own `plugin.json`. */
@Serializable
data class PluginsList(val plugins: List<String>)
