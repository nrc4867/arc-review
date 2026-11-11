package dev.chieppa.arc.ui.plugins.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import dev.chieppa.arc.plugins.api.PropNames
import dev.chieppa.arc.plugins.view.Props
import dev.chieppa.arc.plugins.view.ViewNode
import dev.chieppa.arc.ui.plugins.PluginScope

/** [nodes] one under another; a node with `grow` takes the height the others leave. */
@Composable
fun ColumnScope.PluginChildren(nodes: List<ViewNode>, scope: PluginScope) {
    for (node in nodes) key(node.id) { PluginNode(node, scope, if (grows(node)) Modifier.weight(1f) else Modifier) }
}

/** [nodes] side by side; a node with `grow` takes the width the others leave. */
@Composable
fun RowScope.PluginRowChildren(nodes: List<ViewNode>, scope: PluginScope) {
    for (node in nodes) key(node.id) { PluginNode(node, scope, if (grows(node)) Modifier.weight(1f) else Modifier) }
}

private fun grows(node: ViewNode): Boolean = Props.boolean(node.props, PropNames.GROW) == true
