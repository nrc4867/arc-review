package dev.chieppa.arc.ui.review

import dev.chieppa.arc.core.review.Review
import dev.chieppa.arc.core.source.Source
import dev.chieppa.stretch.frame.PaneEdge
import dev.chieppa.stretch.frame.PaneLayout

/**
 * Switching the active source: each source keeps its own layout, and pinned panes, which belong to the review, follow
 * into every source's layout, split off to the end the first time they arrive.
 */
object SourceSwitching {
    /** The review and views with [source] active and its layout holding every pinned pane. */
    fun activate(review: Review, views: Map<Source, SourceView>, source: Source): Pair<Review, Map<Source, SourceView>> {
        if (source !in review.sources) return review to views
        val view = views[source] ?: SourceView()
        val layout = withPinned(view.layout, review.pinnedIds)
        return review.activate(source) to views + (source to view.copy(layout = layout))
    }

    /** [layout] with each of [pinned] that it lacks, together in one pane split off at the end. */
    fun withPinned(layout: PaneLayout, pinned: List<String>): PaneLayout {
        val missing = pinned.filter { layout.leafOf(it) == null }
        if (missing.isEmpty()) return layout
        val home = pinned.firstOrNull { layout.leafOf(it) != null }?.let { layout.leafOf(it)?.id }
        var result = layout
        val first = missing.first()
        result = if (home != null) {
            result.open(first, home)
        } else if (result.tabs.isEmpty()) {
            result.open(first)
        } else {
            val focusedLeaf = result.focusedLeaf.id
            result.open(first, focusedLeaf).splitWith(first, focusedLeaf, PaneEdge.End)
        }
        val leaf = result.leafOf(first)!!.id
        for (tab in missing.drop(1)) result = result.open(tab, leaf)
        return result
    }

    /** [views] with the pane [id] closed in every source's layout but [except]'s. */
    fun withoutEverywhere(views: Map<Source, SourceView>, id: String, except: Source?): Map<Source, SourceView> =
        views.mapValues { (source, view) -> if (source == except) view else view.copy(layout = view.layout.close(id)) }
}
