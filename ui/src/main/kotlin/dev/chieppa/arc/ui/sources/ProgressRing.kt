package dev.chieppa.arc.ui.sources

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import dev.chieppa.arc.ui.util.ArcSizes
import dev.chieppa.stretch.theme.Stretch

private const val FULL_TURN = 360f
private const val TOP = -90f

/** A small ring filled clockwise from the top by [fraction] (0..1), as review progress shows. */
@Composable
fun ProgressRing(fraction: Float, modifier: Modifier = Modifier, size: Dp = ArcSizes.ring) {
    val c = Stretch.colors
    Canvas(modifier.size(size)) {
        val stroke = ArcSizes.ringStroke.toPx()
        val inset = stroke / 2
        val box = Size(this.size.width - stroke, this.size.height - stroke)
        drawArc(c.track, 0f, FULL_TURN, false, Offset(inset, inset), box, style = Stroke(stroke))
        val sweep = FULL_TURN * fraction.coerceIn(0f, 1f)
        if (sweep > 0f) drawArc(if (fraction >= 1f) c.deltaUp else c.accent, TOP, sweep, false, Offset(inset, inset), box, style = Stroke(stroke))
    }
}
