package dev.chieppa.arc.ui.plugins.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.chieppa.arc.plugins.api.PropNames
import dev.chieppa.arc.plugins.view.Props
import dev.chieppa.arc.plugins.view.ViewNode
import dev.chieppa.arc.ui.plugins.PluginIconNames
import dev.chieppa.arc.ui.plugins.PluginScope
import dev.chieppa.stretch.ui.Button
import dev.chieppa.stretch.ui.ButtonKind

/** A `Button` element as Stretch's button; pressing it calls `onClick`. */
@Composable
fun PluginButton(node: ViewNode, scope: PluginScope, modifier: Modifier = Modifier) {
    val kind = when (Props.string(node.props, PropNames.KIND)) {
        PRIMARY -> ButtonKind.Primary
        QUIET -> ButtonKind.Quiet
        else -> ButtonKind.Default
    }
    Button(
        Props.string(node.props, PropNames.LABEL).orEmpty(),
        { scope.call(node.id, PropNames.ON_CLICK) },
        modifier,
        kind,
        PluginIconNames.of(Props.string(node.props, PropNames.ICON)),
        enabled = Props.boolean(node.props, PropNames.DISABLED) != true,
    )
}

private const val PRIMARY = "primary"
private const val QUIET = "quiet"
