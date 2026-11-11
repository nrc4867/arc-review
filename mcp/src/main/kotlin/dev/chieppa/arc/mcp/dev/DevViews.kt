package dev.chieppa.arc.mcp.dev

import dev.chieppa.arc.mcp.tools.ToolArgs
import dev.chieppa.arc.mcp.tools.ToolRefusal
import dev.chieppa.arc.plugins.protocol.ViewKind
import kotlinx.serialization.json.JsonObject

/** Finds the views of a running plugin's panel or pane that a call names, opening it in the window when asked. */
internal class DevViews(private val host: DevPluginHost, private val window: DevWindow) {
    /** The open views of the panel or pane [args] name, opened first when it asks and none is open. */
    suspend fun find(args: JsonObject): List<DevView> {
        val plugin = DevScope.running(host, ToolArgs.requiredString(args, DevArgs.PLUGIN))
        val view = ToolArgs.requiredString(args, DevArgs.VIEW)
        val kind = when (view) {
            in plugin.panels -> ViewKind.Panel
            in plugin.panes -> ViewKind.Pane
            else -> throw ToolRefusal("${plugin.id} ${DevMessages.NO_VIEW} $view")
        }
        val shown = host.views(plugin.id).filter { it.kind == kind && it.contribution == view && it.nodes.isNotEmpty() }
        if (shown.isNotEmpty()) return shown
        if (ToolArgs.boolean(args, DevArgs.OPEN) != true) throw ToolRefusal("$view ${DevMessages.NOT_SHOWN}")
        window.open(plugin.id, kind, view)
        return listOfNotNull(host.awaitView(plugin.id, view)).ifEmpty { throw ToolRefusal("$view ${DevMessages.NOT_SHOWN}") }
    }
}
