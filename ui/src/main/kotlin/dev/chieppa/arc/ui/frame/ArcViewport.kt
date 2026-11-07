package dev.chieppa.arc.ui.frame

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import dev.chieppa.arc.ui.menus.TabMenu
import dev.chieppa.arc.ui.panes.PageContent
import dev.chieppa.arc.ui.panes.PaneContent
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.arc.ui.workbench.pageTab
import dev.chieppa.arc.ui.workbench.paneTab
import dev.chieppa.stretch.frame.LocalPaneWindows
import dev.chieppa.stretch.frame.PaneGrid
import dev.chieppa.stretch.frame.WindowBounds

/**
 * The viewport: the active source's panes, and every pinned pane and page, such as a saved filter's, in a grid of tabs. Empty while there are none.
 * The tree's files drag in as tabs, onto a pane, an edge or away into a window; a tab's menu holds Arc's items. Where
 * panes can open windows, it keeps where a new one goes: over the viewport, a little in from its corner.
 */
@Composable
fun ArcViewport(workbench: Workbench, modifier: Modifier = Modifier) {
    val host = LocalPaneWindows.current
    PaneGrid(
        layout = workbench.paneLayout,
        onChange = { workbench.paneLayout = it },
        item = { id -> workbench.state.page(id)?.let { pageTab(it, workbench.watch.watchlist) } ?: paneTab(id, workbench.state.pane(id)) },
        modifier = modifier.onGloballyPositioned { coordinates ->
            val topLeft = coordinates.positionInWindow()
            val size = Offset(coordinates.size.width.toFloat(), coordinates.size.height.toFloat())
            workbench.windowBounds = host?.let {
                {
                    val corner = it.screenPosition(topLeft)
                    val far = it.screenPosition(topLeft + size)
                    WindowBounds(corner.x + WINDOW_INSET, corner.y + WINDOW_INSET, far.x - corner.x, far.y - corner.y)
                }
            }
        },
        onClose = workbench.panes::close,
        tabMenu = { id, close -> TabMenu(workbench, id, close) },
        dropTarget = workbench.dropTarget,
        onDropItem = workbench::dropFile,
    ) { id ->
        workbench.state.pane(id)?.let { PaneContent(it, workbench) } ?: workbench.state.page(id)?.let { PageContent(it, workbench) }
    }
}

/** How far in from the viewport's corner a pane opened in a window of its own goes, in dp. */
private const val WINDOW_INSET = 32f
