package dev.chieppa.arc.ui.start

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.chieppa.arc.core.pane.Page
import dev.chieppa.arc.ui.workbench.Workbench

/** A source's home as a tab in the grid: the start screen for that one source, whatever the strip has selected. */
@Composable
fun SourceHomePage(page: Page.Home, workbench: Workbench, modifier: Modifier = Modifier) {
    StartScreen(workbench, modifier, selection = listOf(page.source))
}
