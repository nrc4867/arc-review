package dev.chieppa.arc.ui.plugins.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.chieppa.arc.plugins.api.PropNames
import dev.chieppa.arc.plugins.view.Props
import dev.chieppa.arc.plugins.view.ViewNode
import dev.chieppa.arc.ui.plugins.PluginScope

/** A `Row` element: its children side by side with its gap and padding, aligned across as `align` says. */
@Composable
fun PluginRow(node: ViewNode, scope: PluginScope, modifier: Modifier = Modifier) {
    val align = when (Props.string(node.props, PropNames.ALIGN)) {
        START -> Alignment.Top
        END -> Alignment.Bottom
        else -> Alignment.CenterVertically
    }
    Row(modifier.padding(PluginLayout.padding(node)), horizontalArrangement = Arrangement.spacedBy(PluginLayout.gap(node)), verticalAlignment = align) {
        PluginRowChildren(node.children, scope)
    }
}

private const val START = "start"
private const val END = "end"
