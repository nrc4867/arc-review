package dev.chieppa.arc.ui.runs

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.semantics.clearAndSetSemantics
import dev.chieppa.arc.core.actions.RunState
import dev.chieppa.arc.design.Arc
import dev.chieppa.stretch.theme.Stretch

/**
 * A step's time against its usual one: the track runs to twice the usual 90th percentile, a tick marks the usual, and
 * the fill is the time so far, in green once done, accent while running and the warning color once stalled. A step
 * with no history or not started shows the track alone.
 */
@Composable
fun TimingBar(elapsed: Long?, usual: Long?, state: RunState, stalled: Boolean) {
    val c = Stretch.colors
    val pass = Arc.colors.passEdge
    val fill = when {
        stalled -> c.warnEdge
        state == RunState.Running -> c.accent
        else -> pass
    }
    val tick = c.inkMuted
    val track = c.track
    Canvas(Modifier.size(RunSizes.timingBar, RunSizes.timingBarHeight).clearAndSetSemantics {}) {
        val radius = CornerRadius(size.height / 2, size.height / 2)
        drawRoundRect(track, cornerRadius = radius)
        if (usual == null || usual <= 0) return@Canvas
        val span = usual * 2f
        if (elapsed != null && state != RunState.Queued) {
            drawRoundRect(fill, size = Size(size.width * (elapsed / span).coerceIn(0f, 1f), size.height), cornerRadius = radius)
        }
        val x = size.width / 2
        drawLine(tick, Offset(x, -size.height / 2), Offset(x, size.height * 1.5f), RunSizes.timingTick.toPx())
    }
}
