package dev.chieppa.arc.index.nav

import dev.chieppa.arc.codeintel.model.Confidence
import dev.chieppa.arc.codeintel.model.DefinitionAnswer
import dev.chieppa.arc.codeintel.model.DefinitionRequest
import dev.chieppa.arc.codeintel.model.SymbolTarget
import dev.chieppa.arc.codeintel.provider.DefinitionProvider
import dev.chieppa.arc.core.source.RepoName
import dev.chieppa.arc.index.WorkspaceIndex
import dev.chieppa.arc.index.resolve.Ranked
import dev.chieppa.arc.index.resolve.Resolver
import dev.chieppa.arc.index.symbols.UseSite

/**
 * Definitions from the tree-sitter index: the click's name resolved by [Resolver]. Confident candidates come first;
 * the filtered rest follow, except for a name looked up on a receiver with nothing in scope, which is most often a
 * library member with no source here and is answered with nothing.
 */
class TreeSitterDefinitions(private val index: WorkspaceIndex, private val previews: Previews) : DefinitionProvider {
    override val id = TreeSitterOrigin.PROVIDER
    override val selector = TreeSitterOrigin.selector

    override suspend fun definitions(request: DefinitionRequest): DefinitionAnswer? {
        val document = request.document
        val file = index.file(RepoName(document.repo), document.revision, document.path) ?: return null
        val at = request.at
        file.facts.definitionAt(at.line, at.column)?.let { return DefinitionAnswer(it.label, emptyList(), atDefinition = true) }
        val reference = file.facts.referenceAt(at.line, at.column) ?: return null
        val resolution = Resolver(index.state.value.table).resolve(UseSite(file, reference))
        val showFiltered = resolution.confident.isNotEmpty() || !reference.qualified
        val ranked = resolution.confident.map { it to Confidence.Confident } +
            (if (showFiltered) resolution.filtered.map { it to Confidence.Filtered } else emptyList())
        previews.load(ranked.map { it.first.site.file })
        return DefinitionAnswer(reference.name, ranked.map { (candidate, confidence) -> target(candidate, confidence) })
    }

    private fun target(candidate: Ranked, confidence: Confidence): SymbolTarget {
        val file = candidate.site.file
        val definition = candidate.site.definition
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
