package dev.chieppa.arc.mcp.dev

import dev.chieppa.arc.mcp.tools.ArcTool

/** The plugin development tools on Arc's MCP server, over [host]'s plugins and the [window]. */
object DevTools {
    fun all(host: DevPluginHost, window: DevWindow, timings: DevTimings = DevTimings()): List<ArcTool> = listOf(
        ListPluginsTool(host),
        TypeCheckTool(host),
        ReloadTool(host),
        LogTool(host),
        ViewTreeTool(host, window),
        ScreenshotTool(host, window),
        RunActionTool(host, window, timings),
        FireEventTool(host, window),
    )
}
