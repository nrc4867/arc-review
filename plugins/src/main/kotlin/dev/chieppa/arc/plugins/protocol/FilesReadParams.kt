package dev.chieppa.arc.plugins.protocol

import kotlinx.serialization.Serializable

/** Read the file at [path], absolute or relative to the plugin's folder. */
@Serializable
data class FilesReadParams(val path: String)
