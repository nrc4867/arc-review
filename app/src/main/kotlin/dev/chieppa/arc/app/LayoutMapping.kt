package dev.chieppa.arc.app

import dev.chieppa.arc.store.workspace.StoredPaneLayout
import dev.chieppa.arc.store.workspace.StoredPaneNode
import dev.chieppa.stretch.frame.PaneAxis
import dev.chieppa.stretch.frame.PaneLayout
import dev.chieppa.stretch.frame.PaneLeaf
import dev.chieppa.stretch.frame.PaneNode
import dev.chieppa.stretch.frame.PaneSplit
import dev.chieppa.stretch.frame.returnWindow

/** Turns a pane grid's layout into what the store writes and back. Panes torn out to windows go back into the grid. */
internal object LayoutMapping {
    fun toStored(layout: PaneLayout): StoredPaneLayout {
        val docked = layout.windows.fold(layout) { acc, window -> acc.returnWindow(window.id) }
        return StoredPaneLayout(toStored(docked.root), docked.focused, docked.next)
    }

    /** The stored layout with only the tabs in [panes]; null when it does not make a valid layout. */
    fun toLayout(stored: StoredPaneLayout, panes: Set<String>): PaneLayout? = try {
        val layout = PaneLayout(toNode(stored.root), stored.focused, stored.next)
        layout.tabs.filter { it !in panes }.fold(layout) { acc, tab -> acc.close(tab) }
    } catch (_: IllegalArgumentException) {
        null
    }

    private fun toStored(node: PaneNode): StoredPaneNode = when (node) {
        is PaneLeaf -> StoredPaneNode.Leaf(node.id, node.tabs, node.active)
        is PaneSplit -> StoredPaneNode.Split(node.id, node.axis.name, node.children.map(::toStored), node.weights)
    }

    private fun toNode(node: StoredPaneNode): PaneNode = when (node) {
        is StoredPaneNode.Leaf -> PaneLeaf(node.id, node.tabs, node.active)
        is StoredPaneNode.Split -> PaneSplit(node.id, PaneAxis.valueOf(node.axis), node.children.map(::toNode), node.weights)
    }
}
