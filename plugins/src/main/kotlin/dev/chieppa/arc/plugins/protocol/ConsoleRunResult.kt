package dev.chieppa.arc.plugins.protocol

import kotlinx.serialization.Serializable

/** The process started, by the [handle] its output, input and `Console` components name it with. */
@Serializable
data class ConsoleRunResult(val handle: String)
