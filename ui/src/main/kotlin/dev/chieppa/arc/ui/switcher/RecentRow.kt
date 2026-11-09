package dev.chieppa.arc.ui.switcher

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.chieppa.arc.ui.sources.sourceIcon
import dev.chieppa.arc.ui.util.SourceLabels
import dev.chieppa.stretch.frame.PaletteRow
import dev.chieppa.stretch.theme.Icon
import dev.chieppa.stretch.theme.Stretch

/** A source opened lately and not in the strip now, in the switcher: its kind, title with what [query] matched, ref, and when it was opened. */
@Composable
fun RecentRow(item: SwitcherItem.Recent, query: String, nowSeconds: Long, highlighted: Boolean, onClick: () -> Unit) {
    PaletteRow(highlighted, onClick) {
        Icon(sourceIcon(item.source), Stretch.colors.inkControl)
        if (item.title.isNotEmpty()) RowTitle(item.title, query)
        RowRef(SourceLabels.of(item.source))
        RowMeta("", Modifier.weight(1f))
        RowMeta(agoText(item.at, nowSeconds))
    }
}
