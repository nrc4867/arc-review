package dev.chieppa.arc.plugins.manifest

import kotlinx.serialization.Serializable

/** The folders a plugin's own process may [read] and [write], relative to the plugin's folder or absolute. */
@Serializable
data class FileAccess(val read: List<String> = emptyList(), val write: List<String> = emptyList())
