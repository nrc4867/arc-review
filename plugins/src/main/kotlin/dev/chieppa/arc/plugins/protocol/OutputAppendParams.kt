package dev.chieppa.arc.plugins.protocol

import kotlinx.serialization.Serializable

/** Text a plugin writes to its own channel in Arc's Console, at [level]: `info`, `warn` or `error`. */
@Serializable
data class OutputAppendParams(val text: String, val level: String = OutputLevels.INFO)
