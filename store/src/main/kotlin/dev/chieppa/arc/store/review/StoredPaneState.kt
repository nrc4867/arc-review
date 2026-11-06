package dev.chieppa.arc.store.review

import kotlinx.serialization.Serializable

/** A pane's own state as written: side by side or one column, the line at its top, and its wrapping, null for the setting's. */
@Serializable
data class StoredPaneState(val split: Boolean = true, val line: Int = 0, val wrap: Boolean? = null)
