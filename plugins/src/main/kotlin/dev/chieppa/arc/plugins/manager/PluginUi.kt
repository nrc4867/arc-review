package dev.chieppa.arc.plugins.manager

import dev.chieppa.arc.plugins.protocol.PanesOpenParams

/** What a plugin asks of Arc's window. */
fun interface PluginUi {
    /** Opens [plugin]'s pane as [params] say. */
    suspend fun openPane(plugin: String, params: PanesOpenParams)

    companion object {
        /** No window: panes open nowhere. */
        val NONE = PluginUi { _, _ -> }
    }
}
