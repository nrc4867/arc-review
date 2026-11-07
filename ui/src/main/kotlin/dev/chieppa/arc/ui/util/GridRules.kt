package dev.chieppa.arc.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import dev.chieppa.stretch.theme.Stretch

/** One-stroke lines in [color] along the bottom and end edges, as a table's cells draw between them. */
@Composable
fun Modifier.gridRules(color: Color): Modifier {
    val stroke = Stretch.stroke
    return drawBehind {
        val width = stroke.toPx()
        val y = size.height - width / 2
        val x = size.width - width / 2
        drawLine(color, Offset(0f, y), Offset(size.width, y), width)
        drawLine(color, Offset(x, 0f), Offset(x, size.height), width)
    }
}
