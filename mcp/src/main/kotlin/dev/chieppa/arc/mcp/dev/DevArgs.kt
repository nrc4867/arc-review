package dev.chieppa.arc.mcp.dev

import dev.chieppa.arc.mcp.tools.ToolInput

/** The arguments the development tools share, by name and as their schemas describe them. */
internal object DevArgs {
    const val PLUGIN = "plugin"
    const val VIEW = "view"
    const val OPEN = "open"
    const val SOURCE = "source"
    const val LINES = "lines"
    const val ACTION = "action"
    const val EVENT = "event"
    const val DATA = "data"

    const val DEFAULT_LINES = 100
    const val MAX_LINES = 500

    fun ToolInput.Builder.plugin() = string(PLUGIN, "The plugin's id, as plugins_list gives it.", required = true)

    fun ToolInput.Builder.view() {
        string(VIEW, "The id of one of the plugin's panels or panes, as plugins_list gives them.", required = true)
        boolean(OPEN, "Open the panel's drawer, or the pane on the active source, when it is not open in Arc's window.")
    }

    fun ToolInput.Builder.source() = string(SOURCE, "The ref of a source open in Arc's window, such as api#12; the active source when absent.")
}
