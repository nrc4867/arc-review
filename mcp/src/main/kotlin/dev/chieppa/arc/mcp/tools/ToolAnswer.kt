package dev.chieppa.arc.mcp.tools

/** What a tool call gives: its [parts], and whether it is an [error] the client's model should read as one. */
data class ToolAnswer(val parts: List<ToolPart>, val error: Boolean = false) {
    companion object {
        fun text(text: String): ToolAnswer = ToolAnswer(listOf(ToolPart.Text(text)))

        fun error(text: String): ToolAnswer = ToolAnswer(listOf(ToolPart.Text(text)), error = true)
    }
}
