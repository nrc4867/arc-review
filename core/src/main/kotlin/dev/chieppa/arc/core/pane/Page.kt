package dev.chieppa.arc.core.pane

import dev.chieppa.arc.core.source.Source

/**
 * A tab in the pane grid that shows something other than a file, saved with the review beside its [Pane]s. Pages and
 * panes share one id space, so the grid keys both by [id]. A [pinned] page stays when the active source changes, as a
 * pinned pane does. Each kind of page is a subtype here, and the store and the window handle each kind by its type.
 */
sealed interface Page {
    val id: PaneId
    val pinned: Boolean

    /** The pull requests the saved filter [filter] finds, by the filter's id. It belongs to no source, so it is always pinned. */
    data class Filter(override val id: PaneId, val filter: String) : Page {
        override val pinned: Boolean get() = true
    }

    /** The revision matrix of [source]: its files against the heads it has had. It stays with its source unless [pinned]. */
    data class RevisionMatrix(override val id: PaneId, val source: Source, override val pinned: Boolean = false) : Page

    /** The conversation of pull request [source], open in the grid. It stays with its source unless [pinned]. */
    data class Conversation(override val id: PaneId, val source: Source, override val pinned: Boolean = false) : Page
}
