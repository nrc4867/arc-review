package dev.chieppa.arc.ui.navigation

import dev.chieppa.arc.core.conversation.LineSide

/** Where the caret is in pane [paneId]: [line] from 1, [column] from 0, on [side] of the file at [path]. */
data class PaneCaret(val paneId: String, val line: Int, val column: Int, val path: String? = null, val side: LineSide = LineSide.New)
