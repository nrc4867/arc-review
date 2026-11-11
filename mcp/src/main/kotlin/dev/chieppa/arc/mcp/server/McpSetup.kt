package dev.chieppa.arc.mcp.server

import dev.chieppa.arc.mcp.tools.ArcTool

/**
 * How the app sets up its MCP server: its [token], the [port] it listens on, whether the person left it switched on
 * ([startOn]), Arc's [version] as the server names it, where the person's choice is kept ([onChange]), and how a
 * listening server is made from its tools and token ([endpoint]).
 */
class McpSetup(
    val token: McpToken,
    val port: Int = McpConstants.DEFAULT_PORT,
    val startOn: Boolean = false,
    val version: String,
    val onChange: (Boolean) -> Unit = {},
    val endpoint: (List<ArcTool>, String) -> McpEndpoint = { tools, token -> McpHttpServer(tools, token, version) },
)
