package dev.chieppa.arc.ui.workbench

import dev.chieppa.arc.core.pane.Page
import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.core.watch.Watchlist
import dev.chieppa.arc.design.ArcIcons
import dev.chieppa.stretch.frame.TabItem

/**
 * The tab the pane grid shows for [page]: a saved filter's name under the filter icon, or a revision matrix's title,
 * as [revisionsTitle] gives it for its source, under the matrix icon.
 */
fun pageTab(page: Page, watchlist: Watchlist, revisionsTitle: (Source) -> String): TabItem = when (page) {
    is Page.Filter -> TabItem(page.id.value, watchlist.filter(page.filter)?.name ?: page.filter, ArcIcons.Filter)
    is Page.RevisionMatrix -> TabItem(page.id.value, revisionsTitle(page.source), ArcIcons.Matrix)
}
