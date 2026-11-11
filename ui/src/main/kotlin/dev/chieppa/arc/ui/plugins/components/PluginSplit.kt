package dev.chieppa.arc.ui.plugins.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.chieppa.arc.plugins.api.PropNames
import dev.chieppa.arc.plugins.view.Props
import dev.chieppa.arc.plugins.view.ViewNode
import dev.chieppa.arc.ui.plugins.PluginScope
import dev.chieppa.stretch.ui.Divider
import dev.chieppa.stretch.ui.VerticalDivider

/** A `Split` element: its first two children sharing the room by `ratio`, side by side or stacked, with a divider between. */
@Composable
fun PluginSplit(node: ViewNode, scope: PluginScope, modifier: Modifier = Modifier) {
    val ratio = (Props.number(node.props, PropNames.RATIO) ?: HALF).toFloat().coerceIn(MIN, 1f - MIN)
    val first = node.children.getOrNull(0)
    val second = node.children.getOrNull(1)
    val pane: @Composable (ViewNode?, Modifier) -> Unit = { child, paneModifier ->
        Box(paneModifier) { child?.let { PluginNode(it, scope, Modifier.fillMaxSize()) } }
    }
    if (Props.string(node.props, PropNames.DIRECTION) == VERTICAL) {
        Column(modifier) {
            pane(first, Modifier.weight(ratio))
            Divider()
            pane(second, Modifier.weight(1f - ratio))
        }
    } else {
        Row(modifier) {
            pane(first, Modifier.weight(ratio))
            VerticalDivider()
            pane(second, Modifier.weight(1f - ratio))
        }
    }
}

private const val HALF = 0.5
private const val MIN = 0.05f
private const val VERTICAL = "vertical"
