package dev.chieppa.arc.ui.frame

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.arc.ui.workbench.paneTab
import dev.chieppa.stretch.frame.PaneGrid

/** The viewport: the review's panes in a grid of tabs. A pane's content is drawn by the view for its kind. */
@Composable
fun ArcViewport(workbench: Workbench, modifier: Modifier = Modifier) {
    PaneGrid(
        layout = workbench.paneLayout,
        onChange = { workbench.paneLayout = it },
        item = { id -> paneTab(id, workbench.panes[id]) },
        modifier = modifier,
    ) {}
}
