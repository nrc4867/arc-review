package dev.chieppa.arc.index.hint

import dev.chieppa.arc.codeintel.model.CodeRange
import dev.chieppa.arc.codeintel.model.Confidence
import dev.chieppa.arc.codeintel.model.HoverAnswer
import dev.chieppa.arc.codeintel.model.HoverRequest
import dev.chieppa.arc.codeintel.model.SymbolHint
import dev.chieppa.arc.codeintel.provider.HoverProvider
import dev.chieppa.arc.core.source.RepoName
import dev.chieppa.arc.index.IndexLimits
import dev.chieppa.arc.index.WorkspaceIndex
import dev.chieppa.arc.index.nav.Candidates
import dev.chieppa.arc.index.nav.Previews
import dev.chieppa.arc.index.nav.Targets
import dev.chieppa.arc.index.nav.TreeSitterOrigin
import dev.chieppa.arc.index.symbols.DefSite
import dev.chieppa.arc.syntax.docs.DocComments

/**
 * Symbol hints from the tree-sitter index: for a use, the definitions it resolves to (the confident ones, or the
 * filtered ones when none is), and for a definition's own name, itself; each with its signature and doc comment read
 * from its file's text.
 */
class TreeSitterHovers(private val index: WorkspaceIndex, private val previews: Previews) : HoverProvider {
    override val id = TreeSitterOrigin.PROVIDER
    override val selector = TreeSitterOrigin.selector

    override suspend fun hover(request: HoverRequest): HoverAnswer? {
        val document = request.document
        val file = index.file(RepoName(document.repo), document.revision, document.path) ?: return null
        val at = request.at
        val own = file.facts.definitionAt(at.line, at.column)
        val sites: List<Pair<DefSite, Confidence>>
        val range: CodeRange
        if (own != null) {
            sites = listOf(DefSite(file, own) to Confidence.Confident)
            range = CodeRange.onLine(own.line, own.column, own.nameEnd - own.nameStart)
        } else {
            val reference = file.facts.referenceAt(at.line, at.column) ?: return null
            val ranked = Candidates.of(index, file, reference)
            val confident = ranked.filter { it.second == Confidence.Confident }
            sites = confident.ifEmpty { ranked }.take(IndexLimits.HINT_CANDIDATES).map { it.first.site to it.second }
            range = CodeRange.onLine(reference.line, reference.column, reference.end - reference.start)
        }
        if (sites.isEmpty()) return null
        previews.load(sites.map { it.first.file })
        val hints = sites.mapNotNull { (site, confidence) -> hint(site, confidence) }
        val first = hints.firstOrNull() ?: return null
        return HoverAnswer(HintMarkdown.of(first), TreeSitterOrigin.of(file.grammar.language), range, hints)
    }

    private fun hint(site: DefSite, confidence: Confidence): SymbolHint? {
        val text = previews.text(site.file) ?: return null
        val definition = site.definition
        val grammar = site.file.grammar
        return SymbolHint(
            target = Targets.of(index, previews, site, confidence),
            signature = SignatureReader.lines(grammar, text, definition),
            container = definition.container,
            containerKind = definition.containerKind?.tag,
            doc = DocComments.before(grammar, text, definition.spanStart)?.let(HintDocs::of),
        )
    }
}
