package dev.chieppa.arc.ui.plugins.components

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.chieppa.arc.plugins.api.ComponentNames
import dev.chieppa.arc.plugins.api.PropNames
import dev.chieppa.arc.plugins.view.Props
import dev.chieppa.arc.plugins.view.ViewNode
import dev.chieppa.arc.ui.plugins.PluginScope
import kotlinx.serialization.json.JsonPrimitive

/** A `List` element: its rows built only while on screen; a click on one calls `onSelect` with its index. */
@Composable
fun PluginList(node: ViewNode, scope: PluginScope, modifier: Modifier = Modifier) {
    val selected = Props.int(node.props, PropNames.SELECTED)
    val selectable = Props.handler(node.props, PropNames.ON_SELECT)
    LazyColumn(modifier) {
        itemsIndexed(node.children, key = { _, child -> child.id }) { index, child ->
            val onClick = if (selectable) ({ scope.call(node.id, PropNames.ON_SELECT, JsonPrimitive(index)) }) else null
            if (child.type == ComponentNames.LIST_ROW) PluginListRow(child, index == selected, onClick) else PluginNode(child, scope)
        }
    }
}
