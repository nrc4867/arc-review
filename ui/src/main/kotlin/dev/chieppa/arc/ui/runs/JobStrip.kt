package dev.chieppa.arc.ui.runs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import dev.chieppa.arc.core.actions.RunState
import dev.chieppa.arc.design.Arc
import dev.chieppa.stretch.theme.Stretch

/** One small square per job in its state's color: passed, failed, running, waiting or not started. */
@Composable
fun JobStrip(states: List<RunState>) {
    val c = Stretch.colors
    val shape = Stretch.shapes.ctlShape
    Row(Modifier.clearAndSetSemantics {}, horizontalArrangement = Arrangement.spacedBy(RunSizes.jobSegmentGap)) {
        for (state in states) {
            val (fill, edge) = when (state) {
                RunState.Succeeded -> Arc.colors.passFill to Arc.colors.passEdge
                RunState.Failed -> c.errorWash to c.errorEdge
                RunState.Running -> c.onFill to c.accent
                RunState.Waiting -> c.warnWash to c.warnEdge
                else -> c.surfaceControlHi to c.lineStrong
            }
            Box(Modifier.size(RunSizes.jobSegment).background(fill, shape).border(Stretch.stroke, edge, shape))
        }
    }
}
