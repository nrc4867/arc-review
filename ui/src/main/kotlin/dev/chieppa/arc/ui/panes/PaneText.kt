package dev.chieppa.arc.ui.panes

import dev.chieppa.stretch.diff.FileDiff

/**
 * A pane's file as text: [old] at the pane's base and [new] at its revision, each null where the file is not, and
 * [diff] from one to the other when the pane has a base. A [binary] file has no text on either side.
 */
data class PaneText(val old: String?, val new: String?, val diff: FileDiff? = null, val binary: Boolean = false)
