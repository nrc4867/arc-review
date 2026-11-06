package dev.chieppa.arc.ui.conversation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import dev.chieppa.arc.ui.util.ArcSizes
import dev.chieppa.stretch.theme.Icon
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchSizes
import dev.chieppa.stretch.theme.StretchSpacing

/** A small pill naming a thread's or a staged comment's state, drawn in [look]'s colors. */
@Composable
fun StateChip(text: String, icon: ImageVector, look: StateLook, modifier: Modifier = Modifier) {
    val shape = Stretch.shapes.pillShape
    Row(
        modifier
            .height(StretchSizes.chip)
            .clip(shape)
            .background(look.fill)
            .border(Stretch.stroke, look.edge, shape)
            .padding(horizontal = StretchSpacing.space6),
        horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space4),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, look.ink, size = ArcSizes.ring)
        BasicText(text, style = Stretch.type.chip.merge(TextStyle(color = look.ink)), maxLines = 1)
    }
}
