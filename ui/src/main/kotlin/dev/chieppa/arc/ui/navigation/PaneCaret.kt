package dev.chieppa.arc.ui.navigation

/** Where the caret is in pane [paneId]: [line] from 1, [column] from 0. */
data class PaneCaret(val paneId: String, val line: Int, val column: Int)
