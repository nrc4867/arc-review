package dev.chieppa.arc.plugins.protocol

import kotlinx.serialization.Serializable

/** Start a worker thread for [plugin], the [generation]th Arc started for it, within [limits]. */
@Serializable
data class StartWorkerParams(val plugin: String, val generation: Long, val limits: WorkerLimits = WorkerLimits())
