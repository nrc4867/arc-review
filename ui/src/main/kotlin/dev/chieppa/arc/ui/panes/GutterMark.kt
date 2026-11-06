package dev.chieppa.arc.ui.panes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.chieppa.arc.core.conversation.ThreadState
import dev.chieppa.arc.ui.conversation.stateLook
import dev.chieppa.arc.ui.util.ArcSizes
import dev.chieppa.stretch.theme.Stretch

/** A line's gutter dot for the first thread on it, in its state's color, a ring for a resolved one. */
@Composable
fun GutterMark(thread: ThreadState) {
    val c = Stretch.colors
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        val ink = when (thread) {
            ThreadState.Open -> c.warnInk
            ThreadState.Draft -> c.accent
            else -> stateLook(thread).ink
        }
        val dot = Modifier.size(ArcSizes.gutterDot)
        if (thread == ThreadState.Resolved) Box(dot.border(Stretch.stroke, ink, CircleShape)) else Box(dot.background(ink, CircleShape))
    }
}
