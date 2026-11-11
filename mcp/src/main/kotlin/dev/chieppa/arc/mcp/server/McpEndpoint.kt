package dev.chieppa.arc.mcp.server

import kotlinx.coroutines.flow.StateFlow

/** A listening MCP server as [McpServerControl] runs it: [McpHttpServer], or a test's own. */
interface McpEndpoint {
    /** How many client sessions are open. */
    val clients: StateFlow<Int>

    /** Starts listening on [port] and gives the port it listens on; throws when it cannot. */
    suspend fun start(port: Int): Int

    fun stop()
}
