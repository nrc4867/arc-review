package dev.chieppa.arc.ui.frame

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.chieppa.arc.ui.panes.PaneContent
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.arc.ui.workbench.paneTab
import dev.chieppa.stretch.frame.PaneGrid

/** The viewport: the active source's panes, and every pinned pane, in a grid of tabs. Empty while there are none. */
@Composable
fun ArcViewport(workbench: Workbench, modifier: Modifier = Modifier) {
    PaneGrid(
        layout = workbench.paneLayout,
        onChange = { workbench.paneLayout = it },
        item = { id -> paneTab(id, workbench.state.pane(id)) },
        modifier = modifier,
        onClose = workbench.panes::close,
    ) { id ->
        workbench.state.pane(id)?.let { PaneContent(it, workbench) }
    }
}
