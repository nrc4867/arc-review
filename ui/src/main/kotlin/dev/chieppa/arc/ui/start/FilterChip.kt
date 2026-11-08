package dev.chieppa.arc.ui.start

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import dev.chieppa.arc.design.ArcIcons
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.filters_new_count
import dev.chieppa.stretch.theme.Icon
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchSpacing
import dev.chieppa.stretch.ui.Chip
import dev.chieppa.stretch.ui.ChipKind
import dev.chieppa.stretch.ui.ControlLabel
import org.jetbrains.compose.resources.stringResource

/** A saved filter as a chip: its [name], how many it found ([total], once polled) and how many of them are new. */
@Composable
fun FilterChip(name: String, total: Int?, newCount: Int, onClick: () -> Unit) {
    val c = Stretch.colors
    val shape = Stretch.shapes.pillShape
    val hover = remember { MutableInteractionSource() }
    val hovered by hover.collectIsHoveredAsState()
    Row(
        Modifier
            .height(StartSizes.filterChip)
            .clip(shape)
            .background(if (hovered) c.surfaceControl else Color.Transparent)
            .border(Stretch.stroke, c.chipEdge, shape)
            .hoverable(hover)
            .clickable(hover, null, role = Role.Button, onClick = onClick)
            .padding(start = StretchSpacing.space8, end = StretchSpacing.space10),
        horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space6),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(ArcIcons.Filter, c.inkMuted, size = StartSizes.filterIcon)
        ControlLabel(name, Stretch.type.control, c.inkControl)
        total?.let { ControlLabel(it.toString(), Stretch.type.valueSm, c.inkMuted) }
        if (newCount > 0) Chip(stringResource(Res.string.filters_new_count, newCount), kind = ChipKind.On)
    }
}
