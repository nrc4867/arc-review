package dev.chieppa.arc.ui.panes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import dev.chieppa.arc.ui.util.ArcSizes
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchSpacing

/** A small pill of [text] in [ink] on [fill], edged in [edge], as the pane header marks a file's kind. */
@Composable
fun KindPill(text: String, fill: Color, edge: Color, ink: Color, modifier: Modifier = Modifier) {
    val shape = Stretch.shapes.pillShape
    Box(
        modifier.height(ArcSizes.revisionChipHeight).clip(shape).background(fill).border(Stretch.stroke, edge, shape).padding(horizontal = StretchSpacing.space8),
        contentAlignment = Alignment.Center,
    ) {
        BasicText(text, style = Stretch.type.bodySm.merge(TextStyle(color = ink, fontWeight = FontWeight.SemiBold)), maxLines = 1)
    }
}
