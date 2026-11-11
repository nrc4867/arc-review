package dev.chieppa.arc.plugins.host

/** Starts a plugin's worker. Throws [java.io.IOException] when it cannot start. */
fun interface WorkerLauncher {
    suspend fun launch(spec: WorkerSpec): LineConnection
}
