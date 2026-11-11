package dev.chieppa.arc.index.hint

import dev.chieppa.arc.codeintel.model.Confidence
import dev.chieppa.arc.codeintel.model.DocumentRef
import dev.chieppa.arc.codeintel.model.SymbolTarget
import dev.chieppa.arc.index.WorkspaceIndex
import dev.chieppa.arc.index.nav.Previews
import dev.chieppa.arc.index.nav.Targets
import dev.chieppa.arc.index.symbols.DefSite

/**
 * Where a doc comment's link to another symbol leads: the name as written (`TokenFamily#id`, `RevokeReason.Reuse`,
 * `refresh(String)`) looked up by its last part, preferring a definition in the named container, then the revision
 * and repo of the comment it was written in.
 */
class SymbolLinks(private val index: WorkspaceIndex, private val previews: Previews) {
    suspend fun find(link: String, near: DocumentRef): SymbolTarget? {
        val parts = link.substringBefore('(').split('#', '.').map { it.trim() }.filter { it.isNotEmpty() }
        val name = parts.lastOrNull() ?: return null
        val container = parts.getOrNull(parts.size - 2)
        val best = index.state.value.table.definitions(name, sql = false).maxByOrNull { score(it, container, near) } ?: return null
        previews.load(listOf(best.file))
        return Targets.of(index, previews, best, Confidence.Confident)
    }

    private fun score(site: DefSite, container: String?, near: DocumentRef): Int {
        var score = 0
        if (container != null && site.definition.container == container) score += CONTAINER
        if (site.file.revision == near.revision) score += REVISION
        if (site.file.repo.value == near.repo) score += REPO
        return score
    }

    private companion object {
        const val CONTAINER = 4
        const val REVISION = 2
        const val REPO = 1
    }
}
