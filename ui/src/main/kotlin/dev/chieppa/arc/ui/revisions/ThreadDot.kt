package dev.chieppa.arc.ui.revisions

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.chieppa.arc.core.conversation.ThreadState
import dev.chieppa.arc.design.Arc
import dev.chieppa.arc.ui.util.ArcSizes
import dev.chieppa.stretch.theme.Stretch

/** The dot that stands for a thread in [state]: a ring for an open one, filled for addressed and resolved. */
@Composable
fun ThreadDot(state: ThreadState, modifier: Modifier = Modifier) {
    val c = Stretch.colors
    val base = modifier.size(ArcSizes.threadDot)
    when (state) {
        ThreadState.Addressed -> Box(base.background(Arc.colors.addressed, CircleShape))
        ThreadState.Resolved -> Box(base.background(c.diffAddStrong, CircleShape))
        else -> Box(base.border(ArcSizes.threadDotEdge, c.inkControl, CircleShape))
    }
}
