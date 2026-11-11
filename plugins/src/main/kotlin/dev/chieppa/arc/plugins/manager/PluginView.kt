package dev.chieppa.arc.plugins.manager

import dev.chieppa.arc.plugins.protocol.CallContext
import dev.chieppa.arc.plugins.protocol.PatchOp
import dev.chieppa.arc.plugins.protocol.ViewKind
import dev.chieppa.arc.plugins.view.ViewNode
import dev.chieppa.arc.plugins.view.ViewTree
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject

/**
 * A plugin's panel, pane or settings page while Arc shows it, as [id]: its component tree as the plugin last drew it
 * in [nodes]. Events go back through [onEvent]; [close] when it leaves the window.
 */
class PluginView(
    val id: String,
    val kind: ViewKind,
    val contribution: String,
    val context: CallContext,
    val props: JsonObject,
    private val onEvent: (PluginView, Int, String, JsonArray) -> Unit,
    private val onClose: (PluginView) -> Unit,
) {
    private var tree = ViewTree()
    private val shown = MutableStateFlow<List<ViewNode>>(emptyList())

    /** The view's top-level nodes as they stand. */
    val nodes: StateFlow<List<ViewNode>> get() = shown

    /** Calls node [node]'s [handler] prop in the plugin with [args]. */
    fun event(node: Int, handler: String, args: JsonArray = JsonArray(emptyList())) = onEvent(this, node, handler, args)

    fun close() = onClose(this)

    @Synchronized
    internal fun apply(ops: List<PatchOp>) {
        tree.apply(ops)
        shown.value = tree.snapshot()
    }

    /** Starts over empty, as when the plugin's process starts again and draws the view anew. */
    @Synchronized
    internal fun reset() {
        tree = ViewTree()
        shown.value = emptyList()
    }
}
