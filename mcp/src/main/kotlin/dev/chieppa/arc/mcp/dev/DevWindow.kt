package dev.chieppa.arc.mcp.dev

import dev.chieppa.arc.plugins.protocol.CallContext
import dev.chieppa.arc.plugins.protocol.ViewKind

/** What the development tools ask of Arc's window. */
interface DevWindow {
    /** The context a call on the source [ref] names in the strip carries, or the active source's when null; null when no such source is open. */
    suspend fun context(ref: String?): CallContext?

    /** Opens [plugin]'s panel drawer or pane [contribution] of [kind] in the window, on the active source. */
    suspend fun open(plugin: String, kind: ViewKind, contribution: String)

    /** View [view] as the window draws it, as PNG bytes; null when it is not on screen. */
    suspend fun capture(view: String): ByteArray?

    companion object {
        /** No window: no source, nothing opens, nothing to capture. */
        val NONE = object : DevWindow {
            override suspend fun context(ref: String?): CallContext? = if (ref == null) CallContext() else null

            override suspend fun open(plugin: String, kind: ViewKind, contribution: String) = Unit

            override suspend fun capture(view: String): ByteArray? = null
        }
    }
}
