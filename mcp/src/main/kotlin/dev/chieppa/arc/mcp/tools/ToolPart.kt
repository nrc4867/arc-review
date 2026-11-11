package dev.chieppa.arc.mcp.tools

/** One part of a tool's answer. */
sealed interface ToolPart {
    data class Text(val text: String) : ToolPart

    /** A picture as PNG bytes. */
    class Png(val bytes: ByteArray) : ToolPart
}
