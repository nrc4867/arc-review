package dev.chieppa.arc.plugins.protocol

import kotlinx.serialization.Serializable

/** Write [text] as UTF-8 to the file at [path], absolute or relative to the plugin's folder. */
@Serializable
data class FilesWriteParams(val path: String, val text: String)
