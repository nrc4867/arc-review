package dev.chieppa.arc.plugins.protocol

import kotlinx.serialization.Serializable

/** A heartbeat, numbered by [seq]. */
@Serializable
data class PingParams(val seq: Long)
