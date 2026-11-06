package dev.chieppa.arc.ui.find

import dev.chieppa.arc.core.conversation.LineSide
import dev.chieppa.arc.ui.panes.DiffTextLine
import dev.chieppa.stretch.code.CodeRange
import dev.chieppa.stretch.diff.DiffMatch
import dev.chieppa.stretch.diff.DiffSide

/** Arc's find matches as the viewers draw them in place. */
object ViewerMatches {
    /** [matches] in the [lines] of a one-file diff, each on its line's side. */
    fun inDiff(matches: List<FindMatch>, lines: List<DiffTextLine>): List<DiffMatch> = matches.map { match ->
        val line = lines[match.index]
        DiffMatch(0, sideOf(line.side), line.number, match.start, match.end)
    }

    /** [matches] in a whole text's lines, numbered from 1. */
    fun inCode(matches: List<FindMatch>): List<CodeRange> = matches.map { CodeRange(it.index + 1, it.start, it.end) }

    fun sideOf(side: LineSide): DiffSide = if (side == LineSide.Old) DiffSide.Old else DiffSide.New
}
