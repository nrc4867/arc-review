package dev.chieppa.arc.ui.workbench

import dev.chieppa.arc.core.pane.Pane
import dev.chieppa.stretch.frame.TabItem
import dev.chieppa.stretch.theme.StretchIcons

/** The tab the pane grid shows for [pane], open under [id]: its file's name and a file icon. */
fun paneTab(id: String, pane: Pane?): TabItem = TabItem(id, pane?.fileName ?: id, StretchIcons.File)
