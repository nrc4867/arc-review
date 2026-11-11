package dev.chieppa.arc.plugins.console

import dev.chieppa.arc.plugins.host.LaunchSpec

/** Starts programs for console panes. Throws [java.io.IOException] when the program cannot start. */
fun interface ConsoleLauncher {
    fun launch(spec: LaunchSpec): ConsoleConnection
}
