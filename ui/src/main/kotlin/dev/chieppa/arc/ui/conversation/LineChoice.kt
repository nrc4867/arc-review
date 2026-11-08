package dev.chieppa.arc.ui.conversation

import dev.chieppa.arc.core.conversation.LineSide

/** A line number clicked in pane [paneId] where threads already are: the person picks a new comment or one of them. */
data class LineChoice(val paneId: String, val side: LineSide, val line: Int)
