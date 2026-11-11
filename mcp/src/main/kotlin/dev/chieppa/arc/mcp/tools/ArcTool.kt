package dev.chieppa.arc.mcp.tools

import kotlinx.serialization.json.JsonObject

/**
 * One tool on Arc's MCP server: its [name], the [description] a client's model reads, the JSON schema of its [input],
 * and what calling it with arguments gives. [readOnly] tools change nothing in Arc. A tool refuses what it may not do
 * by throwing [ToolRefusal].
 */
interface ArcTool {
    val name: String
    val description: String
    val input: ToolInput
    val readOnly: Boolean

    suspend fun call(args: JsonObject): ToolAnswer
}
