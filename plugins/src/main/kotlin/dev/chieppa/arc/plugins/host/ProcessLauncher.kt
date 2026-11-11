package dev.chieppa.arc.plugins.host

/** Starts programs. Throws [java.io.IOException] when the program cannot start. */
fun interface ProcessLauncher {
    fun launch(spec: LaunchSpec): LineConnection
}
