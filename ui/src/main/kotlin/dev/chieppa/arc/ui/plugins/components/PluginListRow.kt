package dev.chieppa.arc.ui.plugins.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.chieppa.arc.plugins.api.PropNames
import dev.chieppa.arc.plugins.view.Props
import dev.chieppa.arc.plugins.view.ViewNode
import dev.chieppa.arc.ui.plugins.PluginIconNames
import dev.chieppa.stretch.frame.ListRow
import dev.chieppa.stretch.frame.RowIcon
import dev.chieppa.stretch.frame.RowText
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.ui.Label

/** A `ListRow` element as Stretch's list row: its icon, title and quieter detail; [onClick] when its list takes clicks. */
@Composable
fun PluginListRow(node: ViewNode, selected: Boolean, onClick: (() -> Unit)?, modifier: Modifier = Modifier) {
    ListRow(selected, onClick ?: {}, modifier) {
        PluginIconNames.of(Props.string(node.props, PropNames.ICON))?.let { RowIcon(it) }
        RowText(Props.string(node.props, PropNames.TITLE).orEmpty())
        Props.string(node.props, PropNames.DETAIL)?.let { Label(it, Stretch.type.bodySm, Stretch.colors.inkSubtle) }
    }
}
