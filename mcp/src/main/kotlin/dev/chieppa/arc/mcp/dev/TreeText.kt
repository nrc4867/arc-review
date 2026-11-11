package dev.chieppa.arc.mcp.dev

import dev.chieppa.arc.plugins.protocol.PatchOp
import dev.chieppa.arc.plugins.view.ViewNode
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull

/**
 * A view's component tree as indented text: each element's name, its node id and props, and each run of text quoted,
 * one per line and two spaces deeper per level. A prop holding a handler shows as `<handler>`.
 */
object TreeText {
    private const val INDENT = "  "
    private const val HANDLER = "<handler>"
    private const val ID = "#"

    fun of(nodes: List<ViewNode>): String = buildString { nodes.forEach { write(it, 0) } }.trimEnd()

    private fun StringBuilder.write(node: ViewNode, depth: Int) {
        append(INDENT.repeat(depth))
        if (node.type == ViewNode.TEXT) {
            append(JsonPrimitive(node.text.orEmpty()).toString()).append('\n')
            return
        }
        append(node.type).append(' ').append(ID).append(node.id)
        node.props.forEach { (name, value) -> append(' ').append(name).append('=').append(value(value)) }
        append('\n')
        node.children.forEach { write(it, depth + 1) }
    }

    private fun value(value: JsonElement): String =
        if (value is JsonObject && (value[PatchOp.HANDLER] as? JsonPrimitive)?.booleanOrNull == true) HANDLER else value.toString()
}
