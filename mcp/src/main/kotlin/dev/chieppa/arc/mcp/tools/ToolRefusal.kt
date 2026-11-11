package dev.chieppa.arc.mcp.tools

/** A tool will not do what it was asked, for the reason in [message], which the client's model reads. */
class ToolRefusal(message: String) : Exception(message)
