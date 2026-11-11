package dev.chieppa.arc.mcp.dev

import dev.chieppa.arc.mcp.dev.DevArgs.plugin
import dev.chieppa.arc.mcp.dev.DevArgs.view
import dev.chieppa.arc.mcp.tools.ArcTool
import dev.chieppa.arc.mcp.tools.ToolAnswer
import dev.chieppa.arc.mcp.tools.ToolInput
import dev.chieppa.arc.mcp.tools.ToolPart
import dev.chieppa.arc.mcp.tools.ToolRefusal
import kotlinx.serialization.json.JsonObject

/** A picture of a running plugin's panel or pane as Arc's window draws it. */
class ScreenshotTool(host: DevPluginHost, private val window: DevWindow) : ArcTool {
    private val views = DevViews(host, window)

    override val name = DevToolNames.SCREENSHOT
    override val description = "Takes a PNG of an enabled, running plugin's panel or pane as Arc's window draws it now, only that " +
        "view's area, in the person's theme and size."
    override val input = ToolInput.of {
        plugin()
        view()
    }
    override val readOnly = true

    override suspend fun call(args: JsonObject): ToolAnswer {
        val view = views.find(args).first()
        val png = window.capture(view.id) ?: throw ToolRefusal(DevMessages.NOT_CAPTURED)
        return ToolAnswer(listOf(ToolPart.Png(png), ToolPart.Text("${view.kind.name.lowercase()} ${view.contribution} (${view.id})")))
    }
}
