package dev.chieppa.arc.store.workspace

import kotlinx.serialization.Serializable

/** A pane as written; [view] is a `PaneView` name and [source] a source ref. */
@Serializable
data class StoredPane(
    val id: String,
    val repo: String,
    val path: String,
    val revision: String,
    val view: String,
    val pinned: Boolean = false,
    val base: String? = null,
    val oldPath: String? = null,
    val source: String? = null,
)
