package dev.chieppa.arc.store.workspace

import kotlinx.serialization.Serializable

/**
 * How one source was left: its pane [layout], the tree's open folders, and the commit the tree was scoped to, if
 * any, by id.
 */
@Serializable
data class StoredSourceView(
    val source: String,
    val layout: StoredPaneLayout? = null,
    val expanded: List<String> = emptyList(),
    val commit: String? = null,
)
