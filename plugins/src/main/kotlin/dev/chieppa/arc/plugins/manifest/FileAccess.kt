package dev.chieppa.arc.plugins.manifest

import kotlinx.serialization.Serializable

/** The folders `app.files` may [read] and [write] for a plugin, relative to the plugin's folder or absolute. */
@Serializable
data class FileAccess(val read: List<String> = emptyList(), val write: List<String> = emptyList())
