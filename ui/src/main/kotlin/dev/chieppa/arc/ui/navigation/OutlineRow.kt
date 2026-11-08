package dev.chieppa.arc.ui.navigation

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import dev.chieppa.stretch.frame.PaletteRow
import dev.chieppa.stretch.theme.Icon
import dev.chieppa.stretch.theme.Stretch

/** One outline row: the symbol's icon, its name with its parameters, and its type (a type's kind), indented by depth. */
@Composable
fun OutlineRow(row: OutlineRowItem, current: Boolean, onClick: () -> Unit) {
    val c = Stretch.colors
    val symbol = row.symbol
    PaletteRow(current, onClick, Modifier.heightIn(min = NavigationSizes.outlineRow)) {
        Spacer(Modifier.width(NavigationSizes.outlineIndent * row.depth))
        Icon(SymbolIcons.of(symbol.kind), c.inkControl)
        val name = symbol.parameters?.let { "${symbol.name}(${it.joinToString(", ")})" } ?: symbol.name
        BasicText(name, Modifier.weight(1f, fill = false), style = Stretch.type.body.merge(TextStyle(color = c.ink)), maxLines = 1, overflow = TextOverflow.Ellipsis)
        val detail = symbol.type ?: symbol.kind.takeIf(SymbolIcons::isType)
        if (detail != null) {
            BasicText(detail, style = Stretch.type.valueSm.merge(TextStyle(color = if (current) c.ink else c.inkSubtle)), maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}
