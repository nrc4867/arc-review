package dev.chieppa.arc.plugins.protocol

import kotlinx.serialization.Serializable

/** Process [handle] printed [text] on its standard error when [error], else on its standard output. */
@Serializable
data class ConsoleOutputParams(val handle: String, val text: String, val error: Boolean = false)
