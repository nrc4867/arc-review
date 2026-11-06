package dev.chieppa.arc.ui.workbench

import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.ui.tree.FileTreeItem

/** What an open context menu acts on, so its actions, which the palette also runs, know their target. */
sealed interface MenuTarget {
    /** A source's chip in the strip. */
    data class OfSource(val source: Source) : MenuTarget

    /** A tree row, [item], with the tree's [selection] as it was when the menu opened. */
    data class OfRows(val item: FileTreeItem, val selection: Set<Any>) : MenuTarget

    /** The pane [id], by its tab. */
    data class OfPane(val id: String) : MenuTarget
}
