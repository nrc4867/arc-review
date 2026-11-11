package dev.chieppa.arc.mcp.dev

/**
 * How long the development tools wait before answering: for a reloaded plugin to run or fail ([reloadMillis]), for an
 * action that may be waiting on the person ([actionMillis]), and for an opened view to draw ([drawMillis]).
 */
data class DevTimings(val reloadMillis: Long = RELOAD_MILLIS, val actionMillis: Long = ACTION_MILLIS, val drawMillis: Long = DRAW_MILLIS) {
    private companion object {
        const val RELOAD_MILLIS = 30_000L
        const val ACTION_MILLIS = 10_000L
        const val DRAW_MILLIS = 5_000L
    }
}
