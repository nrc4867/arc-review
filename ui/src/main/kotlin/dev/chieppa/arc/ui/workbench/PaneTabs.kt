package dev.chieppa.arc.ui.workbench

import dev.chieppa.arc.core.pane.Pane
import dev.chieppa.stretch.frame.TabItem
import dev.chieppa.stretch.theme.StretchIcons

/** The tab the pane grid shows for [pane], open under [id]: its file's name with its repo after it, and a pin for a pinned pane. */
fun paneTab(id: String, pane: Pane?): TabItem =
    TabItem(id, pane?.fileName ?: id, if (pane?.pinned == true) StretchIcons.Pin else StretchIcons.File, subtitle = pane?.repo?.value)
