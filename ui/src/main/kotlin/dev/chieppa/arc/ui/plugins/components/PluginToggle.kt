package dev.chieppa.arc.ui.plugins.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.chieppa.arc.plugins.api.PropNames
import dev.chieppa.arc.plugins.view.Props
import dev.chieppa.arc.plugins.view.ViewNode
import dev.chieppa.arc.ui.plugins.PluginScope
import dev.chieppa.stretch.ui.Toggle
import kotlinx.serialization.json.JsonPrimitive

/** A `Toggle` element as Stretch's switch; flipping it calls `onChange` with the new state. */
@Composable
fun PluginToggle(node: ViewNode, scope: PluginScope, modifier: Modifier = Modifier) {
    Toggle(
        Props.boolean(node.props, PropNames.CHECKED) == true,
        { scope.call(node.id, PropNames.ON_CHANGE, JsonPrimitive(it)) },
        Props.string(node.props, PropNames.LABEL).orEmpty(),
        modifier,
    )
}
