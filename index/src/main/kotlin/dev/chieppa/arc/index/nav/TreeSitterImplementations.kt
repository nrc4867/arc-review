package dev.chieppa.arc.index.nav

import dev.chieppa.arc.codeintel.model.Confidence
import dev.chieppa.arc.codeintel.model.ImplementationsAnswer
import dev.chieppa.arc.codeintel.model.ImplementationsRequest
import dev.chieppa.arc.codeintel.provider.ImplementationsProvider
import dev.chieppa.arc.core.source.RepoName
import dev.chieppa.arc.index.WorkspaceIndex
import dev.chieppa.arc.index.symbols.DefSite

/**
 * Implementations from the tree-sitter index, by name: on a type's name, or a use of it, the types that extend or
 * implement it; on a member's, the members overriding it in those types (see [ImplementationFinder]).
 */
class TreeSitterImplementations(private val index: WorkspaceIndex, private val previews: Previews) : ImplementationsProvider {
    override val id = TreeSitterOrigin.PROVIDER
    override val selector = TreeSitterOrigin.selector

    override suspend fun implementations(request: ImplementationsRequest): ImplementationsAnswer? {
        val document = request.document
        val file = index.file(RepoName(document.repo), document.revision, document.path) ?: return null
        val at = request.at
        val sites = file.facts.definitionAt(at.line, at.column)?.let { listOf(DefSite(file, it)) }
            ?: file.facts.referenceAt(at.line, at.column)?.let { reference ->
                Candidates.of(index, file, reference).filter { it.second == Confidence.Confident }.map { it.first.site }
            }
            ?: return null
        val name = sites.firstOrNull()?.definition?.label ?: return null
        val finder = ImplementationFinder(index.state.value.table)
        val found = sites.flatMap { site -> if (site.definition.kind.isType) finder.subtypes(site) else finder.overrides(site) }
            .distinctBy { Triple(it.file.repo, it.file.path, it.definition.nameStart) }
        previews.load(found.map { it.file }.distinct())
        return ImplementationsAnswer(name, found.map { Targets.of(index, previews, it, Confidence.Confident) })
    }
}
