package dev.chieppa.arc.plugins.host

/** Starts plugin processes. Throws [java.io.IOException] when the program cannot start. */
fun interface PluginLauncher {
    fun launch(spec: LaunchSpec): PluginConnection
}
