package dev.chieppa.arc.plugins.protocol

import kotlinx.serialization.Serializable

/** A file in a source by its [path], and the [line] in focus in it, from 1, when there is one. */
@Serializable
data class FileInfo(val path: String, val line: Int? = null)
