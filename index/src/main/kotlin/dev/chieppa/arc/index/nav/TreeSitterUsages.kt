package dev.chieppa.arc.index.nav

import dev.chieppa.arc.codeintel.model.SymbolTarget
import dev.chieppa.arc.codeintel.model.UsageGroup
import dev.chieppa.arc.codeintel.model.UsagesAnswer
import dev.chieppa.arc.codeintel.model.UsagesRequest
import dev.chieppa.arc.codeintel.provider.UsagesProvider
import dev.chieppa.arc.codeintel.router.AnswerMerging
import dev.chieppa.arc.core.source.RepoName
import dev.chieppa.arc.index.WorkspaceIndex
import dev.chieppa.arc.index.symbols.DefSite

/**
 * Usages from the tree-sitter index, found by [UsageFinder] and grouped by the open source that reads them and by
 * language, files the source's change touches first.
 */
class TreeSitterUsages(private val index: WorkspaceIndex, private val previews: Previews) : UsagesProvider {
    override val id = TreeSitterOrigin.PROVIDER
    override val selector = TreeSitterOrigin.selector

    override suspend fun usages(request: UsagesRequest): UsagesAnswer? {
        val document = request.document
        val file = index.file(RepoName(document.repo), document.revision, document.path) ?: return null
        val definition = file.facts.definitionAt(request.at.line, request.at.column) ?: return null
        val state = index.state.value
        val found = UsageFinder(state.table).usages(DefSite(file, definition))
        previews.load(found.map { it.file }.distinct())
        val groups = found.mapNotNull { usage -> state.targetOf(usage.file)?.let { it to usage } }
            .groupBy({ (target, usage) -> target to usage.file.grammar.language }, { it.second })
            .map { (key, usages) ->
                val (target, language) = key
                val targets = usages.map { usage ->
                    SymbolTarget(
                        location = Documents.location(usage.file, usage.line, usage.column, usage.length),
                        name = usage.name,
                        kind = usage.kind,
                        preview = previews.line(usage.file, usage.line),
                        confidence = usage.confidence,
                        origin = TreeSitterOrigin.of(language),
                        source = target.source,
                    )
                }
                UsageGroup(target.source, target.revision, language, TreeSitterOrigin.of(language), AnswerMerging.orderUsages(targets, target.changedPaths))
            }
        return UsagesAnswer(definition.label, groups)
    }
}
