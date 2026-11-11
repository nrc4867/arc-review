package dev.chieppa.arc.mcp.dev

import dev.chieppa.arc.mcp.dev.DevArgs.plugin
import dev.chieppa.arc.mcp.tools.ArcTool
import dev.chieppa.arc.mcp.tools.ToolAnswer
import dev.chieppa.arc.mcp.tools.ToolArgs
import dev.chieppa.arc.mcp.tools.ToolInput
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/** The last lines an enabled directory plugin logged, with its state and why it failed. */
class LogTool(private val host: DevPluginHost) : ArcTool {
    override val name = DevToolNames.LOG
    override val description = "Reads an enabled plugin's log: the last lines it printed (app.log, console.log, console.error and " +
        "uncaught errors with their place in its own files), oldest first, with its state and the problems that stopped or crashed it."
    override val input = ToolInput.of {
        plugin()
        integer(DevArgs.LINES, "How many of the last lines to give; ${DevArgs.DEFAULT_LINES} when absent.", 1, DevArgs.MAX_LINES)
    }
    override val readOnly = true

    @Serializable
    private data class Log(val plugin: String, val state: DevPluginState, val problems: List<DevProblem>, val total: Int, val lines: List<String>)

    override suspend fun call(args: JsonObject): ToolAnswer {
        val plugin = DevScope.reach(host, ToolArgs.requiredString(args, DevArgs.PLUGIN))
        val count = (ToolArgs.int(args, DevArgs.LINES) ?: DevArgs.DEFAULT_LINES).coerceIn(1, DevArgs.MAX_LINES)
        val lines = host.log(plugin.id)
        return ToolAnswer.text(DevJson.encode(Log.serializer(), Log(plugin.id, plugin.state, plugin.problems, lines.size, lines.takeLast(count))))
    }
}
