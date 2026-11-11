package dev.chieppa.arc.index.nav

import dev.chieppa.arc.codeintel.model.DefinitionAnswer
import dev.chieppa.arc.codeintel.model.DefinitionRequest
import dev.chieppa.arc.codeintel.provider.DefinitionProvider
import dev.chieppa.arc.core.source.RepoName
import dev.chieppa.arc.index.WorkspaceIndex

/** Definitions from the tree-sitter index: the click's name resolved as [Candidates] lists them. */
class TreeSitterDefinitions(private val index: WorkspaceIndex, private val previews: Previews) : DefinitionProvider {
    override val id = TreeSitterOrigin.PROVIDER
    override val selector = TreeSitterOrigin.selector

    override suspend fun definitions(request: DefinitionRequest): DefinitionAnswer? {
        val document = request.document
        val file = index.file(RepoName(document.repo), document.revision, document.path) ?: return null
        val at = request.at
        file.facts.definitionAt(at.line, at.column)?.let { return DefinitionAnswer(it.label, emptyList(), atDefinition = true) }
        val reference = file.facts.referenceAt(at.line, at.column) ?: return null
        val ranked = Candidates.of(index, file, reference)
        previews.load(ranked.map { it.first.site.file })
        return DefinitionAnswer(reference.name, ranked.map { (candidate, confidence) -> Targets.of(index, previews, candidate.site, confidence) })
    }
}
