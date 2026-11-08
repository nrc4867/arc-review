package dev.chieppa.arc.core.recent

/** A source the person opened or put away: its [ref], its [title] where it was read, and [at] when (epoch seconds). */
data class RecentSource(val ref: String, val title: String? = null, val at: Long)
