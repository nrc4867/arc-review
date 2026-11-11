package dev.chieppa.arc.plugins.host

import dev.chieppa.arc.plugins.protocol.InitializeParams

/** A plugin made ready to start, or why it cannot be. */
sealed interface Preparation {
    /** Start [worker] and send it [initialize]. */
    data class Ready(val worker: WorkerSpec, val initialize: InitializeParams) : Preparation

    data class Failed(val diagnostics: List<PluginDiagnostic>) : Preparation
}
