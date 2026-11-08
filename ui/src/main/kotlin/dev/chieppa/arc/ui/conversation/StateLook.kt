package dev.chieppa.arc.ui.conversation

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import dev.chieppa.arc.core.conversation.ThreadState
import dev.chieppa.arc.design.Arc
import dev.chieppa.stretch.theme.Stretch

/** The fill, edge and ink a thread or staged comment is drawn in. */
data class StateLook(val fill: Color, val edge: Color, val ink: Color)

/** The colors for a thread in [state]: the guest's for a guest note, the addressed ones for an addressed thread. */
@Composable
fun stateLook(state: ThreadState): StateLook {
    val c = Stretch.colors
    val arc = Arc.colors
    return when (state) {
        ThreadState.Open -> StateLook(Color.Transparent, c.chipEdge, c.inkMuted)
        ThreadState.Addressed -> StateLook(arc.addressedWash, arc.addressedEdge, arc.addressed)
        ThreadState.Resolved -> StateLook(c.diffAddWash, c.diffAddStrong, c.diffAddStrong)
        ThreadState.Draft -> StateLook(Color.Transparent, c.lineStrong, c.ink)
        ThreadState.Staged -> StateLook(c.onFill, c.chipOnEdge, c.onInk)
        ThreadState.Note -> StateLook(Color.Transparent, c.chipEdge, c.inkControl)
        ThreadState.Guest -> StateLook(arc.guestWash, arc.guestEdge, arc.guest)
    }
}
