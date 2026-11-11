package dev.chieppa.arc.mcp.dev

import dev.chieppa.arc.mcp.dev.DevArgs.plugin
import dev.chieppa.arc.mcp.tools.ArcTool
import dev.chieppa.arc.mcp.tools.ToolAnswer
import dev.chieppa.arc.mcp.tools.ToolArgs
import dev.chieppa.arc.mcp.tools.ToolInput
import kotlinx.serialization.json.JsonObject

/** Starts an enabled directory plugin again from its files and says how it stands. */
class ReloadTool(private val host: DevPluginHost) : ArcTool {
    override val name = DevToolNames.RELOAD
    override val description = "Starts an enabled plugin from a folder again from its files, as a save in its folder does, and gives it " +
        "once it runs or has failed: its state, problems, and the actions, panels and panes it offers. Its open views draw again."
    override val input = ToolInput.of { plugin() }
    override val readOnly = false

    override suspend fun call(args: JsonObject): ToolAnswer {
        val plugin = DevScope.reach(host, ToolArgs.requiredString(args, DevArgs.PLUGIN))
        val reloaded = host.reload(plugin.id) ?: return ToolAnswer.error("${plugin.id} ${DevMessages.NOT_SETTLED}")
        return ToolAnswer.text(DevJson.encode(DevPlugin.serializer(), reloaded))
    }
}
