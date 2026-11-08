package dev.chieppa.arc.index

/** What the index holds: how many [sources] and [files], and whether it is reading now. */
data class IndexStatus(val sources: Int = 0, val files: Int = 0, val indexing: Boolean = false)
