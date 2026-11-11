package dev.chieppa.arc.plugins.console

import dev.chieppa.arc.plugins.host.LaunchSpec
import dev.chieppa.arc.plugins.host.SystemProcessLauncher
import kotlinx.coroutines.CoroutineScope

/** Starts programs as real processes, with the environment [SystemProcessLauncher] gives them, read on [scope]. */
class ProcessConsoleLauncher(private val scope: CoroutineScope) : ConsoleLauncher {
    override fun launch(spec: LaunchSpec): ConsoleConnection = ProcessConsoleConnection(SystemProcessLauncher.builder(spec).start(), scope)
}
