package dev.chieppa.arc.mcp.dev

import dev.chieppa.arc.mcp.dev.DevArgs.plugin
import dev.chieppa.arc.mcp.dev.DevArgs.source
import dev.chieppa.arc.mcp.tools.ArcTool
import dev.chieppa.arc.mcp.tools.ToolAnswer
import dev.chieppa.arc.mcp.tools.ToolArgs
import dev.chieppa.arc.mcp.tools.ToolInput
import dev.chieppa.arc.mcp.tools.ToolRefusal
import dev.chieppa.arc.plugins.api.AppApi
import dev.chieppa.arc.plugins.protocol.AppEventParams
import kotlinx.serialization.json.JsonObject

/** Tells one running plugin, and no other, that an event happened in Arc, so its handlers run without the person acting. */
class FireEventTool(private val host: DevPluginHost, private val window: DevWindow) : ArcTool {
    override val name = DevToolNames.FIRE_EVENT
    override val description = "Sends an enabled, running plugin, and only it, an event as if it happened in Arc, so its on handlers " +
        "and app.on listeners run: ${AppApi.eventNames.joinToString(", ")}. The event carries the active source or the open source " +
        "named, and data as the reference describes it. Nothing happens in Arc itself."
    override val input = ToolInput.of {
        plugin()
        string(DevArgs.EVENT, "The event's name.", required = true, oneOf = AppApi.eventNames)
        source()
        obj(DevArgs.DATA, "What else the event carries, such as {\"kind\": \"Reviewed\"} for file.marked.")
    }
    override val readOnly = false

    override suspend fun call(args: JsonObject): ToolAnswer {
        val plugin = DevScope.running(host, ToolArgs.requiredString(args, DevArgs.PLUGIN))
        val event = ToolArgs.requiredString(args, DevArgs.EVENT)
        if (event !in AppApi.eventNames) throw ToolRefusal("${DevMessages.UNKNOWN_EVENT} $event")
        val source = ToolArgs.string(args, DevArgs.SOURCE)
        val context = window.context(source) ?: throw ToolRefusal("${DevMessages.NO_SOURCE} $source")
        val data = ToolArgs.obj(args, DevArgs.DATA) ?: JsonObject(emptyMap())
        if (!host.emit(plugin.id, AppEventParams(event, context, data))) throw ToolRefusal("${plugin.id} ${DevMessages.NOT_HEARD}")
        return ToolAnswer.text("${DevMessages.EVENT_SENT} $event")
    }
}
