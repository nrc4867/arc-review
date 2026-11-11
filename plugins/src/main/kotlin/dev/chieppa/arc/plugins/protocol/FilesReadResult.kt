package dev.chieppa.arc.plugins.protocol

import kotlinx.serialization.Serializable

/** A file's [text], read as UTF-8. */
@Serializable
data class FilesReadResult(val text: String)
