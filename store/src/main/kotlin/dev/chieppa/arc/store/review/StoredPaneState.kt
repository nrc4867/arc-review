package dev.chieppa.arc.store.review

import kotlinx.serialization.Serializable

/** A pane's own state as written: side by side or one column, and the line at its top. */
@Serializable
data class StoredPaneState(val split: Boolean = true, val line: Int = 0)
