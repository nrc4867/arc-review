package dev.chieppa.arc.ui.plugins.components

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.chieppa.arc.plugins.api.PropNames
import dev.chieppa.arc.plugins.view.Props
import dev.chieppa.arc.plugins.view.ViewNode

/** The spacing props of layout elements, in dp. */
object PluginLayout {
    fun gap(node: ViewNode): Dp = (Props.number(node.props, PropNames.GAP) ?: 0.0).dp()

    fun padding(node: ViewNode): Dp = (Props.number(node.props, PropNames.PADDING) ?: 0.0).dp()

    private fun Double.dp(): Dp = coerceAtLeast(0.0).toFloat().dp
}
