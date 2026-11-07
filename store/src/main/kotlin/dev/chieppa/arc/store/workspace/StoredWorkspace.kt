package dev.chieppa.arc.store.workspace

import kotlinx.serialization.Serializable

/**
 * The workspace as written: its sources by ref, the active one, its panes, pages and marks, each source's view, each
 * pane's own state, by pane id, and the heads each source was read at.
 */
@Serializable
data class StoredWorkspace(
    val sources: List<String> = emptyList(),
    val active: String? = null,
    val panes: List<StoredPane> = emptyList(),
    val marks: List<StoredMark> = emptyList(),
    val views: List<StoredSourceView> = emptyList(),
    val paneStates: Map<String, StoredPaneState> = emptyMap(),
    val pages: List<StoredPage> = emptyList(),
    val heads: List<StoredHeads> = emptyList(),
)
