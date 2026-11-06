package dev.chieppa.arc.ui.menus

import dev.chieppa.stretch.core.ActionId
import org.jetbrains.compose.resources.StringResource

/** One menu of the top bar and the macOS menu bar: its title and its actions, a null between groups. */
data class ArcMenu(val title: StringResource, val items: List<ActionId?>) {
    /** The menu without [ids], and without a divider left at either end or beside another. */
    fun without(ids: Set<ActionId>): ArcMenu {
        val kept = items.filter { it == null || it !in ids }
        val tidy = kept.filterIndexed { i, id -> id != null || (i > 0 && kept[i - 1] != null) }
        return copy(items = tidy.dropLastWhile { it == null }.dropWhile { it == null })
    }
}
