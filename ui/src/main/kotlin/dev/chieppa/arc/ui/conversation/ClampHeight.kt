package dev.chieppa.arc.ui.conversation

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp

/**
 * Lays the content out at its whole height and, while [clamped], shows no more than [max] of it. [onOverflow] hears
 * whether the content is taller than [max], so a Show more can appear only when something is cut.
 */
fun Modifier.clampHeight(max: Dp, clamped: Boolean, onOverflow: (Boolean) -> Unit): Modifier = clipToBounds().layout { measurable, constraints ->
    val placeable = measurable.measure(constraints.copy(maxHeight = Constraints.Infinity))
    val limit = max.roundToPx()
    onOverflow(placeable.height > limit)
    val height = if (clamped) minOf(placeable.height, limit) else placeable.height
    layout(placeable.width, height) { placeable.place(0, 0) }
}
