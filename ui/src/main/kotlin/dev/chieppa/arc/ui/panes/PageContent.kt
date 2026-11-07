package dev.chieppa.arc.ui.panes

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.chieppa.arc.core.pane.Page
import dev.chieppa.arc.ui.pulls.FilterPage
import dev.chieppa.arc.ui.workbench.Workbench

/** What a page's tab shows, by its kind. */
@Composable
fun PageContent(page: Page, workbench: Workbench, modifier: Modifier = Modifier) {
    when (page) {
        is Page.Filter -> FilterPage(page, workbench, modifier)
    }
}
