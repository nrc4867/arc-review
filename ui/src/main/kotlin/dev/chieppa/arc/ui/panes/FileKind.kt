package dev.chieppa.arc.ui.panes

import dev.chieppa.arc.core.pane.Pane
import dev.chieppa.stretch.diff.LineKind

/** What a pane's file is, for how it shows: a change with lines to compare, or a file read whole. */
enum class FileKind {
    /** Changed lines on both sides: shown as a diff. */
    Changed,

    /** New in the change: its whole text, every line added. */
    Added,

    /** Gone in the change: its whole former text, every line removed. */
    Deleted,

    /** In the change with no line changed, such as a mode change or a move: its whole text. */
    Unchanged,

    /** Opened at a revision, outside any change. */
    Whole,
    ;

    /** Whether the file has a diff worth showing, rather than only its whole text. */
    val hasDiff: Boolean get() = this == Changed

    companion object {
        fun of(pane: Pane, text: PaneText): FileKind {
            if (!pane.hasChange) return Whole
            val diff = text.diff
            val before = if (text.binary) diff?.oldPath != null else text.old != null
            val after = if (text.binary) diff?.newPath != null else text.new != null
            return when {
                !before && after -> Added
                before && !after -> Deleted
                text.binary -> if (text.binaryChanged) Changed else Unchanged
                diff == null || diff.hunks.none { hunk -> hunk.lines.any { it.kind != LineKind.Context } } -> Unchanged
                else -> Changed
            }
        }
    }
}
