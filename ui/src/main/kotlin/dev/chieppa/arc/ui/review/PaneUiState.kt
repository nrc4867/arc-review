package dev.chieppa.arc.ui.review

/** A pane's own reading state: old and new side by side or in one column, and the line at its top. */
data class PaneUiState(val split: Boolean = true, val line: Int = 0)
