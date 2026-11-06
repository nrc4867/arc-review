package dev.chieppa.arc.design

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

private const val GRID = 16f
private const val STROKE = 1.5f

/** Draws one icon's paths in Stretch's icon style: a 16px grid, 1.5px round strokes, small filled dots. */
internal class IconScope(private val builder: ImageVector.Builder) {
    fun stroke(d: String) {
        builder.addPath(
            pathData = addPathNodes(d),
            stroke = SolidColor(Color.Black),
            strokeLineWidth = STROKE,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        )
    }

    fun fill(d: String) {
        builder.addPath(pathData = addPathNodes(d), fill = SolidColor(Color.Black))
    }
}

internal fun icon(name: String, block: IconScope.() -> Unit): ImageVector {
    val builder = ImageVector.Builder(name, GRID.dp, GRID.dp, GRID, GRID)
    IconScope(builder).block()
    return builder.build()
}
