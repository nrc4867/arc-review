package dev.chieppa.arc.ui.navigation

import dev.chieppa.arc.codeintel.CodeIntel
import dev.chieppa.arc.codeintel.model.DocumentRef
import dev.chieppa.arc.codeintel.model.HintDoc
import dev.chieppa.arc.codeintel.model.SymbolTarget
import dev.chieppa.arc.codeintel.router.CodeIntelRouter
import dev.chieppa.arc.core.source.RepoName
import dev.chieppa.arc.index.IndexStatus
import dev.chieppa.arc.index.IndexTarget
import dev.chieppa.arc.index.WorkspaceIndex
import dev.chieppa.arc.index.hint.FileDocs
import dev.chieppa.arc.index.hint.SymbolLinks
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * What the window asks about code: the providers registered in [intel], asked through [router], and the tree-sitter
 * [index] they read, when there is one, the [links] doc comments make to other symbols, and files' [docs]. Without an index, nothing
 * is indexed and no name is clickable.
 */
class CodeNavigation(
    val intel: CodeIntel = CodeIntel(),
    private val index: WorkspaceIndex? = null,
    private val links: SymbolLinks? = null,
    private val docs: FileDocs? = null,
) {
    val router = CodeIntelRouter(intel)

    /** What the index holds, for the status bar. */
    val status: StateFlow<IndexStatus> = index?.status ?: MutableStateFlow(IndexStatus())

    /** Indexes the open sources [targets]. Call off the UI thread. */
    suspend fun index(targets: List<IndexTarget>) {
        index?.update(targets)
    }

    /** Where a doc comment's link to [name], written in [near], leads; null when nothing here defines it. Call off the UI thread. */
    suspend fun symbolLink(name: String, near: DocumentRef): SymbolTarget? = links?.find(name, near)

    /** The doc comments of [document], whose content is [text], read; empty without an index. Call off the UI thread. */
    suspend fun docs(document: DocumentRef, text: String): List<HintDoc> = docs?.of(document, text).orEmpty()

    /** The name at [line], [column] of [document]: the use or definition there, or null. Call off the UI thread. */
    suspend fun nameAt(document: DocumentRef, line: Int, column: Int): NameSpan? {
        val file = index?.file(RepoName(document.repo), document.revision, document.path) ?: return null
        file.facts.referenceAt(line, column)?.let { return NameSpan(it.line, it.column, it.column + (it.end - it.start)) }
        file.facts.definitionAt(line, column)?.let { return NameSpan(it.line, it.column, it.column + (it.nameEnd - it.nameStart)) }
        return null
    }
}
