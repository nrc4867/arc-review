package dev.chieppa.arc.ui.pulls

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import dev.chieppa.arc.host.github.pulls.PullLabel
import dev.chieppa.arc.ui.util.ArcSizes
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchSpacing

/** A GitHub label as a pill: a dot in the label's own color, then its name. */
@Composable
fun LabelPill(label: PullLabel, modifier: Modifier = Modifier) {
    val c = Stretch.colors
    val shape = Stretch.shapes.pillShape
    Row(
        modifier.height(ArcSizes.labelPill).border(Stretch.stroke, c.chipEdge, shape).padding(horizontal = StretchSpacing.space6),
        horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space4),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(ArcSizes.labelDot).background(LabelColors.of(label.color, c.inkMuted), CircleShape))
        BasicText(label.name, style = Stretch.type.chip.merge(TextStyle(color = c.inkControl)), maxLines = 1)
    }
}
