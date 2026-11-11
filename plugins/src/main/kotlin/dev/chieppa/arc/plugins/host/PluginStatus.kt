package dev.chieppa.arc.plugins.host

import dev.chieppa.arc.plugins.protocol.Contributions

/** Where a plugin's process stands. */
sealed interface PluginStatus {
    /** Not enabled, or stopped. */
    data object Off : PluginStatus

    /** Transpiling, starting or loading its bundle. */
    data object Starting : PluginStatus

    /** Running and contributing [contributions]. */
    data class Running(val contributions: Contributions) : PluginStatus

    /** Ended unexpectedly [crashes] times lately and waiting to start again. */
    data class Restarting(val crashes: Int) : PluginStatus

    /** Stopped until its files change or it is enabled again, for [diagnostics]. */
    data class Failed(val diagnostics: List<PluginDiagnostic>) : PluginStatus
}
