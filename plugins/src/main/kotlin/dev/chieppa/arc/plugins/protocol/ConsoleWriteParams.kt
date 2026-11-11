package dev.chieppa.arc.plugins.protocol

import kotlinx.serialization.Serializable

/** Write [text] to process [handle]'s input. */
@Serializable
data class ConsoleWriteParams(val handle: String, val text: String)
