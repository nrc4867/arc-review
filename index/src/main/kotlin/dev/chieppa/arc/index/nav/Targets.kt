package dev.chieppa.arc.index.nav

import dev.chieppa.arc.codeintel.model.Confidence
import dev.chieppa.arc.codeintel.model.SymbolTarget
import dev.chieppa.arc.index.WorkspaceIndex
import dev.chieppa.arc.index.symbols.DefSite

/** Definitions as answers point at them. */
internal object Targets {
    /** [site] as a target, its line read from [previews], signed with the source that reads its revision. */
    fun of(index: WorkspaceIndex, previews: Previews, site: DefSite, confidence: Confidence): SymbolTarget {
        val file = site.file
        val definition = site.definition
        return SymbolTarget(
            location = Documents.location(file, definition.line, definition.column, definition.nameEnd - definition.nameStart),
            name = definition.label,
            kind = definition.kind.tag,
            preview = previews.line(file, definition.line),
            confidence = confidence,
            origin = TreeSitterOrigin.of(file.grammar.language),
            source = index.state.value.targetOf(file)?.source,
        )
    }
}
