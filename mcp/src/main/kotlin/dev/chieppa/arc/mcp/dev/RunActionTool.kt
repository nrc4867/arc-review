package dev.chieppa.arc.mcp.dev

import dev.chieppa.arc.mcp.dev.DevArgs.plugin
import dev.chieppa.arc.mcp.dev.DevArgs.source
import dev.chieppa.arc.mcp.tools.ArcTool
import dev.chieppa.arc.mcp.tools.ToolAnswer
import dev.chieppa.arc.mcp.tools.ToolArgs
import dev.chieppa.arc.mcp.tools.ToolInput
import dev.chieppa.arc.mcp.tools.ToolRefusal
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.json.JsonObject

/**
 * Runs one of a running plugin's actions as the palette would, with no more than the person's own click: whatever Arc
 * confirms with the person is still confirmed in the window. It answers once the action returns, or after
 * [DevTimings.actionMillis] that it is still running, rather than waiting on the person.
 */
class RunActionTool(private val host: DevPluginHost, private val window: DevWindow, private val timings: DevTimings = DevTimings()) : ArcTool {
    override val name = DevToolNames.RUN_ACTION
    override val description = "Runs one of an enabled, running plugin's actions, as the person would from the palette, on the active " +
        "source or the open source named. It answers once the action returns, or says it is still running when it waits on the " +
        "person, such as a confirmation in Arc's window; it never confirms anything itself."
    override val input = ToolInput.of {
        plugin()
        string(DevArgs.ACTION, "The action's id, as plugins_list gives it.", required = true)
        source()
    }
    override val readOnly = false

    override suspend fun call(args: JsonObject): ToolAnswer {
        val plugin = DevScope.running(host, ToolArgs.requiredString(args, DevArgs.PLUGIN))
        val action = ToolArgs.requiredString(args, DevArgs.ACTION)
        if (action !in plugin.actions) throw ToolRefusal("${plugin.id} ${DevMessages.NO_ACTION} $action")
        val source = ToolArgs.string(args, DevArgs.SOURCE)
        val context = window.context(source) ?: throw ToolRefusal("${DevMessages.NO_SOURCE} $source")
        val ran = try {
            withTimeoutOrNull(timings.actionMillis) { host.runAction(plugin.id, action, context) }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            return ToolAnswer.error("${DevMessages.ACTION_FAILED} ${e.message ?: e}")
        }
        return ToolAnswer.text(if (ran == null) DevMessages.ACTION_WAITING else DevMessages.ACTION_DONE)
    }
}
