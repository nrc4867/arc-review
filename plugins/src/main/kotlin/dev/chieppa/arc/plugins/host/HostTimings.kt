package dev.chieppa.arc.plugins.host

/** How a plugin host times its processes. */
data class HostTimings(
    /** How long a request may wait for its answer. */
    val requestMillis: Long = REQUEST_MILLIS,
    /** How long the plugin has to load its bundle and answer `initialize`. */
    val initializeMillis: Long = INITIALIZE_MILLIS,
    /** How often a running plugin is pinged; one that misses a ping's answer is hung and is ended. */
    val heartbeatMillis: Long = HEARTBEAT_MILLIS,
    /** How long a plugin that ended waits before it starts again. */
    val restartMillis: Long = RESTART_MILLIS,
    /** How many times a plugin may end within [crashWindowMillis] before it is left stopped as failed. */
    val maxCrashes: Int = MAX_CRASHES,
    val crashWindowMillis: Long = CRASH_WINDOW_MILLIS,
) {
    private companion object {
        const val REQUEST_MILLIS = 10_000L
        const val INITIALIZE_MILLIS = 20_000L
        const val HEARTBEAT_MILLIS = 5_000L
        const val RESTART_MILLIS = 1_000L
        const val MAX_CRASHES = 3
        const val CRASH_WINDOW_MILLIS = 60_000L
    }
}
