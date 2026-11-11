package dev.chieppa.arc.store.settings

/** The keys [SettingsFile] writes each setting under. */
internal object SettingsKeys {
    const val THEME = "theme"
    const val INTERFACE_SIZE = "interface.size"
    const val COMMENT = "Arc settings"
    const val LAST_REVIEW = "review.last"
    const val WRAP_LINES = "panes.wrap"
    const val LINE_HIGHLIGHT = "panes.highlight.lines"
    const val MCP_SERVER = "mcp.server"
    const val MCP_PORT = "mcp.port"
    const val MAX_PORT = 65_535
}
