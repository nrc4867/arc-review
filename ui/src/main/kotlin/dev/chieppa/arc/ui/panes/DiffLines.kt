package dev.chieppa.arc.ui.panes

import dev.chieppa.arc.core.conversation.LineSide
import dev.chieppa.stretch.diff.FileDiff
import dev.chieppa.stretch.diff.LineKind

/** The lines of a diff, top to bottom, each once: a removed line on the old side, every other line on the new. */
object DiffLines {
    fun of(file: FileDiff): List<DiffTextLine> = file.hunks.flatMap { hunk ->
        hunk.lines.mapNotNull { line ->
            if (line.kind == LineKind.Removed) {
                line.oldNumber?.let { DiffTextLine(LineSide.Old, it, line.text) }
            } else {
                line.newNumber?.let { DiffTextLine(LineSide.New, it, line.text) }
            }
        }
    }

    /** The new-side numbers of the lines [file] adds. */
    fun added(file: FileDiff): List<Int> = file.hunks.flatMap { hunk -> hunk.lines.filter { it.kind == LineKind.Added }.mapNotNull { it.newNumber } }
}
