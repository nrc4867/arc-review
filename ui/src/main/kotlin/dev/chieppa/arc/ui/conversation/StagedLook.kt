package dev.chieppa.arc.ui.conversation

import androidx.compose.runtime.Composable
import dev.chieppa.stretch.theme.Stretch

/** The colors for a staged comment: on, or the warning colors when its line is gone. */
@Composable
fun stagedLook(lineGone: Boolean): StateLook {
    val c = Stretch.colors
    return if (lineGone) StateLook(c.warnWash, c.warnEdge, c.warnInk) else StateLook(c.onFill, c.chipOnEdge, c.onInk)
}
