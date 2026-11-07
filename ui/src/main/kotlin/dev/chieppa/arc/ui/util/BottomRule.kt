package dev.chieppa.arc.ui.util

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import dev.chieppa.stretch.theme.Stretch
import androidx.compose.runtime.Composable

/** A one-stroke line in [color] along the bottom edge, as list rows draw between them. */
@Composable
fun Modifier.bottomRule(color: Color): Modifier {
    val stroke = Stretch.stroke
    return drawBehind {
        val y = size.height - stroke.toPx() / 2
        drawLine(color, Offset(0f, y), Offset(size.width, y), stroke.toPx())
    }
}
