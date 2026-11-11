package dev.chieppa.arc.index.nav

import dev.chieppa.arc.codeintel.CodeIntel
import dev.chieppa.arc.index.WorkspaceIndex
import dev.chieppa.arc.index.hint.FileDocs
import dev.chieppa.arc.index.hint.SymbolLinks
import dev.chieppa.arc.index.hint.TreeSitterHovers
import dev.chieppa.arc.index.store.BlobStore

/** Registers the tree-sitter index's answers (definitions, usages, outlines, symbol hints) in [intel]; closing takes them out. */
class TreeSitterNavigation(index: WorkspaceIndex, store: BlobStore) {
    private val previews = Previews(store)
    val definitions = TreeSitterDefinitions(index, previews)
    val usages = TreeSitterUsages(index, previews)
    val outline = TreeSitterOutline(index)
    val hovers = TreeSitterHovers(index, previews)

    /** Where doc comments' links to other symbols lead. */
    val links = SymbolLinks(index, previews)

    /** Files' doc comments, read, for drawing them in place. */
    val docs = FileDocs(index)

    fun register(intel: CodeIntel): AutoCloseable {
        val registrations = listOf(intel.definitions.register(definitions), intel.usages.register(usages), intel.outlines.register(outline), intel.hovers.register(hovers))
        return AutoCloseable { registrations.forEach(AutoCloseable::close) }
    }
}
