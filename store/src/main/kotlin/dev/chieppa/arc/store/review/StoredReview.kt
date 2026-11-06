package dev.chieppa.arc.store.review

import kotlinx.serialization.Serializable

/**
 * A review as written: its sources by ref, the active one, its panes and marks, each source's view and each pane's
 * own state, by pane id.
 */
@Serializable
data class StoredReview(
    val id: String,
    val name: String,
    val sources: List<String> = emptyList(),
    val active: String? = null,
    val panes: List<StoredPane> = emptyList(),
    val marks: List<StoredMark> = emptyList(),
    val views: List<StoredSourceView> = emptyList(),
    val paneStates: Map<String, StoredPaneState> = emptyMap(),
)
