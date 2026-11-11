package dev.chieppa.arc.ui.mcp

import java.util.concurrent.ConcurrentHashMap

/** How each plugin view on screen draws itself into a PNG, by view id, while it shows. */
class ViewCaptures {
    private val captures = ConcurrentHashMap<String, suspend () -> ByteArray?>()

    fun register(view: String, capture: suspend () -> ByteArray?) {
        captures[view] = capture
    }

    fun remove(view: String) {
        captures.remove(view)
    }

    /** View [view] drawn as a PNG; null when it is not on screen. Call on the window's thread. */
    suspend fun capture(view: String): ByteArray? = captures[view]?.invoke()
}
