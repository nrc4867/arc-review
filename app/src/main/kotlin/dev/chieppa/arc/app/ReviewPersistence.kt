package dev.chieppa.arc.app

import dev.chieppa.arc.core.change.ObjectId
import dev.chieppa.arc.core.review.Review
import dev.chieppa.arc.core.review.ReviewId
import dev.chieppa.arc.core.source.RepoName
import dev.chieppa.arc.store.review.ReviewMapping
import dev.chieppa.arc.store.review.StoredPaneState
import dev.chieppa.arc.store.review.StoredReview
import dev.chieppa.arc.store.review.StoredSourceView
import dev.chieppa.arc.ui.review.PaneUiState
import dev.chieppa.arc.ui.review.ReviewState
import dev.chieppa.arc.ui.review.SourceView
import dev.chieppa.arc.ui.tree.TreeKeys
import java.nio.file.Path

/** Turns the open review's state into a stored review and back, with each source's view and each pane's state. */
internal object ReviewPersistence {
    fun toState(stored: StoredReview, repos: Map<RepoName, Path>): ReviewState {
        val review = ReviewMapping.toReview(stored)
        val panes = review.tabIds
        val views = stored.views.mapNotNull { view ->
            val source = review.sources.firstOrNull { it.ref == view.source } ?: return@mapNotNull null
            val layout = view.layout?.let { LayoutMapping.toLayout(it, panes) }
            val commit = view.commit?.let { runCatching { ObjectId(it) }.getOrNull() }
            source to SourceView(layout ?: dev.chieppa.stretch.frame.PaneLayout(), view.expanded.toSet(), commit)
        }.toMap()
        val paneStates = stored.paneStates.filterKeys { it in panes }.mapValues { (_, s) -> PaneUiState(s.split, s.line, s.wrap) }
        return ReviewState(review, repos, views, paneStates)
    }

    fun toStored(state: ReviewState, expanded: Set<Any>): StoredReview {
        val keys = expanded.filterIsInstance<String>()
        val views = state.review.sources.map { source ->
            val view = state.viewOf(source)
            StoredSourceView(
                source = source.ref,
                layout = LayoutMapping.toStored(view.layout),
                expanded = keys.filter { TreeKeys.belongsTo(it, source) },
                commit = view.commit?.value,
            )
        }
        val paneStates = state.review.panes.associate { pane ->
            val id = pane.id.value
            id to state.paneState(id).let { StoredPaneState(it.split, state.paneLines[id] ?: 0, it.wrap) }
        }
        return ReviewMapping.toStored(state.review, views, paneStates)
    }

    /** A new, empty review named [name] with an id no stored review has. */
    fun newReview(name: String, taken: Set<String>): Review {
        val id = generateSequence(1) { it + 1 }.map { "$ID_PREFIX$it" }.first { it !in taken }
        return Review(ReviewId(id), name)
    }

    private const val ID_PREFIX = "review-"
}
