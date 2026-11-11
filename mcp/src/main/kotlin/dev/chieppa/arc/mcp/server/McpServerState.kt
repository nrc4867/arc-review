package dev.chieppa.arc.mcp.server

/** Where Arc's MCP server stands. */
sealed interface McpServerState {
    /** Switched off, as it is until the person switches it on. */
    data object Off : McpServerState

    data object Starting : McpServerState

    /** Listening on 127.0.0.1:[port], with [clients] sessions open. */
    data class Listening(val port: Int, val clients: Int) : McpServerState

    /** Switched on but not listening, for [reason], such as its port being taken. */
    data class Failed(val reason: String) : McpServerState
}
