package dev.chieppa.arc.ui.navigation

import dev.chieppa.arc.codeintel.model.OutlineSymbol

/** One outline row: a [symbol] at [depth]. */
data class OutlineRowItem(val symbol: OutlineSymbol, val depth: Int)

/** An outline as the drawer lists it: depth first, keeping the symbols whose name holds a filter and what they are in. */
object OutlineRows {
    fun of(symbols: List<OutlineSymbol>, filter: String): List<OutlineRowItem> = buildList {
        fun visit(symbol: OutlineSymbol, depth: Int): Boolean {
            val at = size
            add(OutlineRowItem(symbol, depth))
            val childMatched = symbol.children.map { visit(it, depth + 1) }.any { it }
            val matched = filter.isBlank() || symbol.name.contains(filter, ignoreCase = true) || childMatched
            if (!matched) subList(at, size).clear()
            return matched
        }
        symbols.forEach { visit(it, 0) }
    }

    /** The row the caret at [line] is in: the last one starting at or above it. */
    fun current(rows: List<OutlineRowItem>, line: Int?): OutlineRowItem? =
        line?.let { at -> rows.lastOrNull { it.symbol.range.start.line <= at } }
}
