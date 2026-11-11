package dev.chieppa.arc.mcp.dev

import dev.chieppa.arc.mcp.dev.DevArgs.plugin
import dev.chieppa.arc.mcp.dev.DevArgs.view
import dev.chieppa.arc.mcp.tools.ArcTool
import dev.chieppa.arc.mcp.tools.ToolAnswer
import dev.chieppa.arc.mcp.tools.ToolInput
import kotlinx.serialization.json.JsonObject

/** The component tree a running plugin's panel or pane draws now, as indented text. */
class ViewTreeTool(host: DevPluginHost, window: DevWindow) : ArcTool {
    private val views = DevViews(host, window)

    override val name = DevToolNames.VIEW_TREE
    override val description = "Reads the component tree an enabled, running plugin's panel or pane draws in Arc's window now, as " +
        "indented text: each element's name and node id (#n) with its props as JSON, each run of text quoted on its own line, and " +
        "a prop holding a function shown as <handler>. A pane opened more than once gives each open view."
    override val input = ToolInput.of {
        plugin()
        view()
    }
    override val readOnly = true

    override suspend fun call(args: JsonObject): ToolAnswer =
        ToolAnswer.text(views.find(args).joinToString("\n\n") { "${it.kind.name.lowercase()} ${it.contribution} (${it.id})\n${TreeText.of(it.nodes)}" })
}
