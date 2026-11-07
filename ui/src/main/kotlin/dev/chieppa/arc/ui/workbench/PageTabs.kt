package dev.chieppa.arc.ui.workbench

import dev.chieppa.arc.core.pane.Page
import dev.chieppa.arc.core.watch.Watchlist
import dev.chieppa.arc.design.ArcIcons
import dev.chieppa.stretch.frame.TabItem

/** The tab the pane grid shows for [page]: a saved filter's name under the filter icon. */
fun pageTab(page: Page, watchlist: Watchlist): TabItem = when (page) {
    is Page.Filter -> TabItem(page.id.value, watchlist.filter(page.filter)?.name ?: page.filter, ArcIcons.Filter)
}
