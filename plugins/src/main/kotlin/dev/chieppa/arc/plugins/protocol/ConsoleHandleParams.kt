package dev.chieppa.arc.plugins.protocol

import kotlinx.serialization.Serializable

/** The process named by [handle]. */
@Serializable
data class ConsoleHandleParams(val handle: String)
