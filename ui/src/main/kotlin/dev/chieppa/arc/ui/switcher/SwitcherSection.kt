package dev.chieppa.arc.ui.switcher

/** The groups the switcher's results come in, in the order they show. */
enum class SectionKind { InReview, Files, Followed }

/** One group of results: the [items] that show, best first, out of [total] that match. */
data class SwitcherSection(val kind: SectionKind, val items: List<SwitcherItem>, val total: Int)
