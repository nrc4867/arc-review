package dev.chieppa.arc.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import dev.chieppa.stretch.theme.Stretch

/** A one-stroke line in [color] along the top edge, as a section over another draws. */
@Composable
fun Modifier.topRule(color: Color): Modifier {
    val stroke = Stretch.stroke
    return drawBehind {
        val y = stroke.toPx() / 2
        drawLine(color, Offset(0f, y), Offset(size.width, y), stroke.toPx())
    }
}
