package dev.chieppa.arc.ui.plugins.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.chieppa.arc.plugins.api.PropNames
import dev.chieppa.arc.plugins.view.Props
import dev.chieppa.arc.plugins.view.ViewNode
import dev.chieppa.arc.ui.plugins.PluginScope
import dev.chieppa.stretch.frame.Dropdown
import kotlinx.serialization.json.JsonPrimitive

/** A `Select` element as Stretch's dropdown; choosing calls `onChange` with the option. */
@Composable
fun PluginSelect(node: ViewNode, scope: PluginScope, modifier: Modifier = Modifier) {
    val options = Props.strings(node.props, PropNames.OPTIONS)
    if (options.isEmpty()) return
    val value = Props.string(node.props, PropNames.VALUE)?.takeIf { it in options } ?: options.first()
    Dropdown(options, value, { scope.call(node.id, PropNames.ON_CHANGE, JsonPrimitive(it)) }, { it }, Props.string(node.props, PropNames.LABEL).orEmpty(), modifier)
}
