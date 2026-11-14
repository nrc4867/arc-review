package dev.chieppa.arc.ui.frame

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.conversation_tab
import dev.chieppa.arc.design.generated.revisions_tab
import dev.chieppa.arc.design.generated.run_tab
import dev.chieppa.arc.design.generated.settings_title
import dev.chieppa.arc.design.generated.runs_tab
import dev.chieppa.arc.core.actions.RunRef
import dev.chieppa.arc.ui.menus.TabMenu
import dev.chieppa.arc.ui.panes.PageContent
import dev.chieppa.arc.ui.panes.PaneContent
import dev.chieppa.arc.ui.start.StartScreen
import dev.chieppa.arc.ui.util.SourceLabels
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.arc.ui.workbench.pageTab
import dev.chieppa.arc.ui.workbench.paneTab
import dev.chieppa.stretch.frame.LocalPaneWindows
import dev.chieppa.stretch.frame.PaneGrid
import dev.chieppa.stretch.frame.WindowBounds
import org.jetbrains.compose.resources.stringResource

/**
 * The viewport: the active source's panes, and every pinned pane and page, such as a saved filter's, in a grid of tabs.
 * While there are none, the start screen shows over the grid. The tree's files drag in as tabs, onto a pane, an edge or
 * away into a window, the start screen showing or not; a tab's menu holds Arc's items, and a tab held after a crash
 * reloads through [dev.chieppa.arc.ui.review.PaneController.reload]. Where panes can open windows, it keeps where a new
 * one goes: over the viewport, a little in from its corner.
 */
@Composable
fun ArcViewport(workbench: Workbench, modifier: Modifier = Modifier) {
    val host = LocalPaneWindows.current
    val revisionsTitle = stringResource(Res.string.revisions_tab)
    val titleOf = { source: Source -> revisionsTitle.replace(TITLE_ARG, SourceLabels.of(source)) }
    val conversationTitle = stringResource(Res.string.conversation_tab)
    val conversationOf = { source: Source -> conversationTitle.replace(TITLE_ARG, SourceLabels.of(source)) }
    val runsTab = stringResource(Res.string.runs_tab)
    val runsOf = { id: String -> runsTab.replace(TITLE_ARG, workbench.runs.watch.watchlist.filter(id)?.name ?: id) }
    val settingsTitle = stringResource(Res.string.settings_title)
    val runTab = stringResource(Res.string.run_tab)
    val runOf = { ref: RunRef -> workbench.runs.watch.run(ref)?.let { runTab.replace(NAME_ARG, it.workflowName).replace(NUMBER_ARG, it.number.toString()) } ?: ref.toString() }
    Box(modifier) {
        PaneGrid(
            layout = workbench.paneLayout,
            onChange = { workbench.paneLayout = it },
            item = { id -> workbench.state.page(id)?.let { pageTab(it, workbench.watch.watchlist, titleOf, conversationOf, runsOf, runOf, settingsTitle, workbench.plugins::tabOf) } ?: paneTab(id, workbench.state.pane(id)) },
            modifier = Modifier.fillMaxSize().onGloballyPositioned { coordinates ->
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
            onReload = workbench.panes::reload,
        ) { id ->
            workbench.state.pane(id)?.let { PaneContent(it, workbench) } ?: workbench.state.page(id)?.let { PageContent(it, workbench) }
        }
        if (workbench.paneLayout.leaves.all { it.tabs.isEmpty() }) StartScreen(workbench)
    }
}

/** Where a revision matrix's tab title takes its source. */
private const val TITLE_ARG = "%1${'$'}s"
private const val NAME_ARG = "%1${'$'}s"
private const val NUMBER_ARG = "%2${'$'}d"

/** How far in from the viewport's corner a pane opened in a window of its own goes, in dp. */
private const val WINDOW_INSET = 32f
