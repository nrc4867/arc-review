package dev.chieppa.arc.plugins.host

import kotlinx.coroutines.CoroutineScope

/** Starts plugin processes as real programs, reading their streams on [scope]. */
class ProcessLauncher(private val scope: CoroutineScope) : PluginLauncher {
    override fun launch(spec: LaunchSpec): PluginConnection {
        val builder = ProcessBuilder(spec.command).directory(spec.workingDirectory.toFile())
        builder.environment().putAll(spec.environment)
        return ProcessConnection(builder.start(), scope)
    }
}
