package dev.chieppa.arc.plugins.protocol

import kotlinx.serialization.Serializable

/** One line a search found: the file's [path], the [line] from 1 and its [text]. */
@Serializable
data class GrepHit(val path: String, val line: Int, val text: String)
