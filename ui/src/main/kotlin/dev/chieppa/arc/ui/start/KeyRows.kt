package dev.chieppa.arc.ui.start

import androidx.compose.runtime.Composable
import dev.chieppa.arc.core.start.StartRow
import dev.chieppa.arc.core.start.StartTrailing
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.core.ActionId
import dev.chieppa.stretch.frame.LocalActionText

/** A row for each of [ids] that has keys in [workbench]'s keymap: the action's label and its keys as bound now. */
@Composable
fun keyRows(workbench: Workbench, ids: List<ActionId>): List<StartRow> {
    val text = LocalActionText.current
    return ids.filter { workbench.keymap.keysFor(it).isNotEmpty() }.map { StartRow(text(it.value), trailing = StartTrailing.Keys(it.value)) }
}
