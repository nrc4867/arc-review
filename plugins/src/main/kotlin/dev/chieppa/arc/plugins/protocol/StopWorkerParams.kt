package dev.chieppa.arc.plugins.protocol

import kotlinx.serialization.Serializable

/** End [plugin]'s worker thread. */
@Serializable
data class StopWorkerParams(val plugin: String)
