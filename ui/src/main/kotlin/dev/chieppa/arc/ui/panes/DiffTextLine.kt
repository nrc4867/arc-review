package dev.chieppa.arc.ui.panes

import dev.chieppa.arc.core.conversation.LineSide

/** One line of a diff as find and threads see it: its side, its number on that side, and its text. */
data class DiffTextLine(val side: LineSide, val number: Int, val text: String)
