package dev.chieppa.arc.ui.review

/**
 * A pane's own reading state: old and new side by side or in one column, the line at its top, and whether long lines
 * wrap, or null to follow the setting.
 */
data class PaneUiState(val split: Boolean = true, val line: Int = 0, val wrap: Boolean? = null, val docs: Boolean = false)
