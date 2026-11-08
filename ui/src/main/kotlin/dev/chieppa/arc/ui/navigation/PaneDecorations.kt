package dev.chieppa.arc.ui.navigation

import dev.chieppa.arc.codeintel.model.CodeDecoration
import dev.chieppa.arc.codeintel.model.CodeRange as ContractRange
import dev.chieppa.arc.codeintel.model.DocumentRef
import dev.chieppa.stretch.code.CodeDecorationStyle
import dev.chieppa.stretch.code.CodeRange

/**
 * The decorations a file draws: the names the last navigation marked in it and what providers decorate it with, each
 * range cut into one piece per line, as the viewers draw them.
 */
object PaneDecorations {
    fun of(document: DocumentRef, focus: List<FocusMark>, provided: List<CodeDecoration>, lineLength: (Int) -> Int?): List<LineDecoration> {
        val marked = focus.filter { same(it.location.document, document) }.flatMap { cut(it.location.range, DecorationStyles.of(it.style), null, lineLength) }
        val decorated = provided.flatMap { cut(it.range, DecorationStyles.of(it.style), it.hover, lineLength) }
        return marked + decorated
    }

    private fun same(a: DocumentRef, b: DocumentRef) = a.repo == b.repo && a.revision == b.revision && a.path == b.path

    private fun cut(range: ContractRange, style: CodeDecorationStyle, tooltip: String?, lineLength: (Int) -> Int?): List<LineDecoration> {
        val first = range.start.line
        val last = range.end.line
        return (first..last).mapNotNull { line ->
            val length = lineLength(line) ?: return@mapNotNull null
            val start = if (line == first) range.start.column else 0
            val end = if (line == last) range.end.column else length
            if (end > start) LineDecoration(CodeRange(line, start, minOf(end, length)), style, tooltip) else null
        }
    }
}
