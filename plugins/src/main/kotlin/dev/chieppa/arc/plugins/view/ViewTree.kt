package dev.chieppa.arc.plugins.view

import dev.chieppa.arc.plugins.protocol.PatchOp
import kotlinx.serialization.json.JsonObject

/**
 * A plugin view's component tree, built up from the patches its process sends. Not thread safe: one view's patches are
 * applied in order on one thread. A patch naming a node the tree lacks is skipped, so a bad patch cannot break the view.
 */
class ViewTree {
    private class Node(val id: Int, val type: String, var props: JsonObject, var text: String?) {
        val children = mutableListOf<Int>()
        var parent: Int? = null
    }

    private val nodes = HashMap<Int, Node>().apply { put(PatchOp.ROOT, Node(PatchOp.ROOT, ROOT_TYPE, JsonObject(emptyMap()), null)) }

    /** Applies [ops] in order. */
    fun apply(ops: List<PatchOp>) {
        ops.forEach(::apply)
    }

    /** The view's top-level nodes as they stand. */
    fun snapshot(): List<ViewNode> = nodes.getValue(PatchOp.ROOT).children.mapNotNull(::build)

    /** How many nodes the tree holds, the view itself not counted. */
    val size: Int get() = nodes.size - 1

    private fun apply(op: PatchOp) {
        when (op) {
            is PatchOp.Create -> nodes[op.id] = Node(op.id, op.type, op.props, null)
            is PatchOp.CreateText -> nodes[op.id] = Node(op.id, ViewNode.TEXT, JsonObject(emptyMap()), op.text)
            is PatchOp.SetProps -> nodes[op.id]?.props = op.props
            is PatchOp.SetText -> nodes[op.id]?.text = op.text
            is PatchOp.Append -> place(op.parent, op.child, null)
            is PatchOp.Insert -> place(op.parent, op.child, op.before)
            is PatchOp.Remove -> remove(op.parent, op.child)
        }
    }

    private fun place(parentId: Int, childId: Int, before: Int?) {
        val parent = nodes[parentId] ?: return
        val child = nodes[childId] ?: return
        child.parent?.let { nodes[it]?.children?.remove(childId) }
        val at = before?.let { parent.children.indexOf(it) }?.takeIf { it >= 0 } ?: parent.children.size
        parent.children.add(at, childId)
        child.parent = parentId
    }

    private fun remove(parentId: Int, childId: Int) {
        nodes[parentId]?.children?.remove(childId)
        forget(childId)
    }

    private fun forget(id: Int) {
        val node = nodes.remove(id) ?: return
        node.children.forEach(::forget)
    }

    private fun build(id: Int): ViewNode? {
        val node = nodes[id] ?: return null
        return ViewNode(node.id, node.type, node.props, node.children.mapNotNull(::build), node.text)
    }

    private companion object {
        const val ROOT_TYPE = "#root"
    }
}
