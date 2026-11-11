package dev.chieppa.arc.plugins.protocol

import kotlinx.serialization.Serializable

/** Process [handle] ended with exit [code]. */
@Serializable
data class ConsoleExitParams(val handle: String, val code: Int)
