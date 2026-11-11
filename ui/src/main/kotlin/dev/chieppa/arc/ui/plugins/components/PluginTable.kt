package dev.chieppa.arc.ui.plugins.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.chieppa.arc.plugins.api.PropNames
import dev.chieppa.arc.plugins.view.Props
import dev.chieppa.arc.plugins.view.ViewNode
import dev.chieppa.arc.ui.plugins.PluginScope
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchSizes
import dev.chieppa.stretch.theme.StretchSpacing
import dev.chieppa.stretch.ui.Divider
import dev.chieppa.stretch.ui.Label
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive

/**
 * A `Table` element: its headings over its rows of cells in equal columns, drawn in Stretch's tokens; a click on a
 * row calls `onSelect` with its index.
 */
@Composable
fun PluginTable(node: ViewNode, scope: PluginScope, modifier: Modifier = Modifier) {
    val c = Stretch.colors
    val columns = Props.strings(node.props, PropNames.COLUMNS)
    val rows = Props.array(node.props, PropNames.ROWS).map { row -> (row as? JsonArray).orEmpty().map { (it as? JsonPrimitive)?.content.orEmpty() } }
    val selectable = Props.handler(node.props, PropNames.ON_SELECT)
    Column(modifier.fillMaxWidth()) {
        TableLine(columns, Modifier) { Label(it, Stretch.type.eyebrow, c.inkMuted) }
        Divider()
        rows.forEachIndexed { index, cells ->
            val click = if (selectable) Modifier.clickable { scope.call(node.id, PropNames.ON_SELECT, JsonPrimitive(index)) } else Modifier
            TableLine(columns.indices.map { cells.getOrElse(it) { "" } }, click) { Label(it, Stretch.type.valueSm, c.ink) }
            Divider()
        }
    }
}

@Composable
private fun TableLine(cells: List<String>, modifier: Modifier, cell: @Composable (String) -> Unit) {
    Row(
        modifier.fillMaxWidth().heightIn(min = StretchSizes.row).padding(horizontal = StretchSpacing.space8),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        cells.forEach { text -> Row(Modifier.weight(1f)) { cell(text) } }
    }
}
