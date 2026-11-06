package dev.chieppa.arc.ui.find

/** One match: in the line at [index] of the lines searched, from [start] up to [end]. */
data class FindMatch(val index: Int, val start: Int, val end: Int)
