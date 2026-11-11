package dev.chieppa.arc.plugins.manager

import dev.chieppa.arc.plugins.notes.PluginNotes
import dev.chieppa.arc.plugins.protocol.PanesOpenParams

/** What a plugin asks of Arc's window. */
interface PluginUi {
    /** Opens [plugin]'s pane as [params] say. */
    suspend fun openPane(plugin: String, params: PanesOpenParams)

    /** The person's notes and comment drafts. */
    val notes: PluginNotes get() = PluginNotes.NONE

    companion object {
        /** No window: panes open nowhere, and there are no notes. */
        val NONE = object : PluginUi {
            override suspend fun openPane(plugin: String, params: PanesOpenParams) = Unit
        }
    }
}
