package dev.chieppa.arc.index.nav

import dev.chieppa.arc.codeintel.model.Confidence
import dev.chieppa.arc.index.IndexedFile
import dev.chieppa.arc.index.WorkspaceIndex
import dev.chieppa.arc.index.resolve.Ranked
import dev.chieppa.arc.index.resolve.Resolver
import dev.chieppa.arc.index.symbols.UseSite
import dev.chieppa.arc.syntax.facts.Reference

/**
 * The definitions a use may mean, as navigation lists them: confident ones first, then the filtered rest, except for
 * a name looked up on a receiver with nothing in scope, which is most often a library member with no source here and
 * gets none.
 */
internal object Candidates {
    fun of(index: WorkspaceIndex, file: IndexedFile, reference: Reference): List<Pair<Ranked, Confidence>> {
        val resolution = Resolver(index.state.value.table).resolve(UseSite(file, reference))
        val showFiltered = resolution.confident.isNotEmpty() || !reference.qualified
        return resolution.confident.map { it to Confidence.Confident } +
            (if (showFiltered) resolution.filtered.map { it to Confidence.Filtered } else emptyList())
    }
}
