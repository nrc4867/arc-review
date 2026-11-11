package dev.chieppa.arc.mcp.dev

import dev.chieppa.arc.mcp.tools.ArcTool
import dev.chieppa.arc.mcp.tools.ToolAnswer
import dev.chieppa.arc.mcp.tools.ToolInput
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.JsonObject

/** Lists the person's directory plugins with their state, problems and log size. */
class ListPluginsTool(private val host: DevPluginHost) : ArcTool {
    override val name = DevToolNames.LIST
    override val description = "Lists the plugins Arc loads from folders on this machine, enabled or not: each one's id, title, folder, " +
        "state (off, waiting for approval, unreadable, starting, running, restarting, failed), problems with their place, how many type " +
        "errors it has, how many lines its log holds, and the actions, panels and panes it offers while it runs. The other plugin " +
        "tools reach only those the person has enabled. Plugins from git sources are not listed."
    override val input = ToolInput()
    override val readOnly = true

    override suspend fun call(args: JsonObject): ToolAnswer = ToolAnswer.text(DevJson.encode(ListSerializer(DevPlugin.serializer()), DevScope.listed(host)))
}
