package dev.chieppa.arc.ui.start

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.chieppa.stretch.core.ActionId
import dev.chieppa.stretch.frame.caps
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.ui.Kbd

/** The key caps of the first chord bound to [action] in [workbench]'s keymap as it is now; nothing when none is bound. */
@Composable
fun ActionKeys(workbench: Workbench, action: String, modifier: Modifier = Modifier) {
    val chord = workbench.keymap.keysFor(ActionId(action)).firstOrNull() ?: return
    Kbd(chord.caps(), modifier)
}
