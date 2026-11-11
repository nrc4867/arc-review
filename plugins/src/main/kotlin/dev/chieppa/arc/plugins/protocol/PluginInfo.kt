package dev.chieppa.arc.plugins.protocol

import kotlinx.serialization.Serializable

/** The plugin a process runs: its [id], [version] and the [folder] it was loaded from. */
@Serializable
data class PluginInfo(val id: String, val version: String, val folder: String)
