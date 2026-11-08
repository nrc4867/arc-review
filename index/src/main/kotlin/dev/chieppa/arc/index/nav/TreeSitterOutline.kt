package dev.chieppa.arc.index.nav

import dev.chieppa.arc.codeintel.model.CodeRange
import dev.chieppa.arc.codeintel.model.OutlineAnswer
import dev.chieppa.arc.codeintel.model.OutlineRequest
import dev.chieppa.arc.codeintel.model.OutlineSymbol
import dev.chieppa.arc.codeintel.provider.OutlineProvider
import dev.chieppa.arc.core.source.RepoName
import dev.chieppa.arc.index.WorkspaceIndex
import dev.chieppa.arc.syntax.facts.Definition

/** A file's outline from the tree-sitter index: its definitions nested as written, local ones left out. */
class TreeSitterOutline(private val index: WorkspaceIndex) : OutlineProvider {
    override val id = TreeSitterOrigin.PROVIDER
    override val selector = TreeSitterOrigin.selector

    override suspend fun outline(request: OutlineRequest): OutlineAnswer? {
        val document = request.document
        val file = index.file(RepoName(document.repo), document.revision, document.path) ?: return null
        return OutlineAnswer(symbols(file.facts.definitions), TreeSitterOrigin.of(file.grammar.language))
    }

    companion object {
        /** [definitions] as an outline tree: each under its parent, local ones (inside a function) left out. */
        fun symbols(definitions: List<Definition>): List<OutlineSymbol> {
            val children = definitions.indices.groupBy { definitions[it].parent }
            fun build(index: Int): OutlineSymbol? {
                val definition = definitions[index]
                if (definition.isLocal) return null
                return OutlineSymbol(
                    name = definition.label,
                    kind = definition.kind.tag,
                    range = CodeRange.onLine(definition.line, definition.column, definition.nameEnd - definition.nameStart),
                    parameters = definition.parameters,
                    type = definition.type,
                    children = children[index].orEmpty().mapNotNull(::build),
                )
            }
            return children[-1].orEmpty().mapNotNull(::build)
        }
    }
}
