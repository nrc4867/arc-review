package dev.chieppa.arc.mcp.dev

import dev.chieppa.arc.mcp.dev.DevArgs.plugin
import dev.chieppa.arc.mcp.tools.ArcTool
import dev.chieppa.arc.mcp.tools.ToolAnswer
import dev.chieppa.arc.mcp.tools.ToolArgs
import dev.chieppa.arc.mcp.tools.ToolInput
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.JsonObject

/** Type checks an enabled directory plugin and gives every error with its place. */
class TypeCheckTool(private val host: DevPluginHost) : ArcTool {
    override val name = DevToolNames.TYPE_CHECK
    override val description = "Type checks an enabled plugin from a folder with Arc's TypeScript and the plugin API's types, as Arc " +
        "does after each save, and gives every error: file (relative to the plugin's folder), line and column counted from 1, and " +
        "message. A plugin runs whatever its types say, so check before reloading."
    override val input = ToolInput.of { plugin() }
    override val readOnly = true

    override suspend fun call(args: JsonObject): ToolAnswer {
        val plugin = DevScope.reach(host, ToolArgs.requiredString(args, DevArgs.PLUGIN))
        val errors = host.typeCheck(plugin.id).map(DevProblem::of)
        return ToolAnswer.text(if (errors.isEmpty()) DevMessages.NO_ERRORS else DevJson.encode(ListSerializer(DevProblem.serializer()), errors))
    }
}
