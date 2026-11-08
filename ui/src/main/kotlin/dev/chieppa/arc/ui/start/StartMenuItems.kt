package dev.chieppa.arc.ui.start

import androidx.compose.runtime.Composable
import dev.chieppa.arc.core.start.StartMenuItem
import dev.chieppa.arc.ui.actions.actionIcon
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.frame.MenuItem

/** A contributor's menu [items] that can run now, each running its action and closing the menu. */
@Composable
fun StartMenuItems(workbench: Workbench, items: List<StartMenuItem>, close: () -> Unit) {
    for (item in items.filter { StartRunner.available(workbench, it.action) }) {
        MenuItem(
            text = item.label,
            onClick = {
                StartRunner.run(workbench, item.action)
                close()
            },
            icon = actionIcon(item.action.id),
        )
    }
}
