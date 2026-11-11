package dev.chieppa.arc.mcp.server

/** The command that adds Arc's MCP server to Claude Code, with its address and token, as Settings shows it. */
object ConnectCommand {
    fun of(port: Int, token: String): String =
        "claude mcp add --transport http ${McpConstants.NAME} ${url(port)} --header \"Authorization: Bearer $token\""

    /** The server's endpoint on [port]. */
    fun url(port: Int): String = "http://${McpConstants.HOST}:$port${McpConstants.PATH}"
}
