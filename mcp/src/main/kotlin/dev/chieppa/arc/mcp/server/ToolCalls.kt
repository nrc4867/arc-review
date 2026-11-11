package dev.chieppa.arc.mcp.server

import dev.chieppa.arc.mcp.tools.ArcTool
import dev.chieppa.arc.mcp.tools.ToolAnswer
import dev.chieppa.arc.mcp.tools.ToolPart
import dev.chieppa.arc.mcp.tools.ToolRefusal
import io.modelcontextprotocol.kotlin.sdk.types.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.types.ImageContent
import io.modelcontextprotocol.kotlin.sdk.types.TextContent
import io.modelcontextprotocol.kotlin.sdk.types.Tool
import io.modelcontextprotocol.kotlin.sdk.types.ToolAnnotations
import io.modelcontextprotocol.kotlin.sdk.types.ToolSchema
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.JsonObject
import java.util.Base64

/** Arc's tools as the MCP SDK lists and calls them: a refusal or failure answers as an error result the model reads. */
internal object ToolCalls {
    private const val PNG = "image/png"
    private const val FAILED = "The tool failed:"

    fun describe(tool: ArcTool): Tool = Tool(
        name = tool.name,
        inputSchema = ToolSchema(properties = tool.input.properties, required = tool.input.required.ifEmpty { null }),
        description = tool.description,
        annotations = ToolAnnotations(readOnlyHint = tool.readOnly, destructiveHint = false, openWorldHint = false),
    )

    suspend fun call(tool: ArcTool, args: JsonObject?): CallToolResult = result(
        try {
            tool.call(args ?: JsonObject(emptyMap()))
        } catch (e: CancellationException) {
            throw e
        } catch (e: ToolRefusal) {
            ToolAnswer.error(e.message.orEmpty())
        } catch (e: Exception) {
            ToolAnswer.error("$FAILED ${e.message ?: e}")
        },
    )

    private fun result(answer: ToolAnswer): CallToolResult = CallToolResult(
        content = answer.parts.map { part ->
            when (part) {
                is ToolPart.Text -> TextContent(part.text)
                is ToolPart.Png -> ImageContent(Base64.getEncoder().encodeToString(part.bytes), PNG)
            }
        },
        isError = answer.error.takeIf { it },
    )
}
