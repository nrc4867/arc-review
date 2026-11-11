package dev.chieppa.arc.plugins.protocol

import kotlinx.serialization.Serializable

/** [plugin]'s worker of [generation] ended with [code]; [error] is what ended it when it failed. */
@Serializable
data class WorkerExitParams(val plugin: String, val generation: Long, val code: Int, val error: String? = null)
