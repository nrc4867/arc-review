package dev.chieppa.arc.ui.pulls

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import dev.chieppa.arc.core.watch.SavedFilter
import dev.chieppa.arc.design.ArcIcons
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.filters_new_count
import dev.chieppa.stretch.frame.ContextMenuArea
import dev.chieppa.stretch.theme.Icon
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchSpacing
import dev.chieppa.stretch.ui.Chip
import dev.chieppa.stretch.ui.ChipKind
import org.jetbrains.compose.resources.stringResource

/**
 * A saved filter in the Pull requests drawer: its name with how many of its pull requests are new, its query under it,
 * and how many are open. The [selected] filter, the focused tab's, is in the on colors; a right-click opens [menu].
 * [total] is null until the filter is polled.
 */
@Composable
fun FilterListRow(
    filter: SavedFilter,
    newCount: Int,
    total: Int?,
    selected: Boolean,
    onClick: () -> Unit,
    menu: @Composable ColumnScope.(close: () -> Unit) -> Unit,
) {
    val c = Stretch.colors
    val source = remember { MutableInteractionSource() }
    val hovered by source.collectIsHoveredAsState()
    val shape = Stretch.shapes.rowShape
    val ink = if (selected) c.onInk else c.ink
    ContextMenuArea(menu) {
        Row(
            Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(if (selected) c.onFill else if (hovered) c.surfaceControl else Color.Transparent)
                .border(Stretch.stroke, if (selected) c.onEdge else Color.Transparent, shape)
                .hoverable(source)
                .clickable(source, null, role = Role.Button, onClick = onClick)
                .semantics { this.selected = selected }
                .padding(horizontal = StretchSpacing.space8, vertical = StretchSpacing.space6),
            horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space8),
        ) {
            Icon(ArcIcons.Filter, ink, Modifier.padding(top = StretchSpacing.space2))
            Column(Modifier.weight(1f)) {
                Row(horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space6), verticalAlignment = Alignment.CenterVertically) {
                    BasicText(filter.name, Modifier.weight(1f, fill = false), style = Stretch.type.body.merge(TextStyle(color = ink, fontWeight = FontWeight.Medium)), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    if (newCount > 0) {
                        val text = stringResource(Res.string.filters_new_count, newCount)
                        if (selected) {
                            BasicText(text, style = Stretch.type.chip.merge(TextStyle(color = ink, fontWeight = FontWeight.Normal)))
                        } else {
                            Chip(text, kind = ChipKind.On)
                        }
                    }
                }
                BasicText(
                    filter.query,
                    style = Stretch.type.valueSm.merge(TextStyle(color = if (selected) ink else c.inkSubtle)),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (total != null) BasicText(total.toString(), style = Stretch.type.valueSm.merge(TextStyle(color = ink)))
        }
    }
}
