package dev.chieppa.arc.index.nav

import dev.chieppa.arc.codeintel.CodeIntel
import dev.chieppa.arc.index.WorkspaceIndex
import dev.chieppa.arc.index.store.BlobStore

/** Registers the tree-sitter index's answers (definitions, usages, outlines) in [intel]; closing takes them out. */
class TreeSitterNavigation(index: WorkspaceIndex, store: BlobStore) {
    private val previews = Previews(store)
    val definitions = TreeSitterDefinitions(index, previews)
    val usages = TreeSitterUsages(index, previews)
    val outline = TreeSitterOutline(index)

    fun register(intel: CodeIntel): AutoCloseable {
        val registrations = listOf(intel.definitions.register(definitions), intel.usages.register(usages), intel.outlines.register(outline))
        return AutoCloseable { registrations.forEach(AutoCloseable::close) }
    }
}
