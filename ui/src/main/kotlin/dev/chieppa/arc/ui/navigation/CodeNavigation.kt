package dev.chieppa.arc.ui.navigation

import dev.chieppa.arc.codeintel.CodeIntel
import dev.chieppa.arc.codeintel.model.DocumentRef
import dev.chieppa.arc.codeintel.router.CodeIntelRouter
import dev.chieppa.arc.core.source.RepoName
import dev.chieppa.arc.index.IndexStatus
import dev.chieppa.arc.index.IndexTarget
import dev.chieppa.arc.index.WorkspaceIndex
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * What the window asks about code: the providers registered in [intel], asked through [router], and the tree-sitter
 * [index] they read, when there is one. Without an index, nothing is indexed and no name is clickable.
 */
class CodeNavigation(val intel: CodeIntel = CodeIntel(), private val index: WorkspaceIndex? = null) {
    val router = CodeIntelRouter(intel)

    /** What the index holds, for the status bar. */
    val status: StateFlow<IndexStatus> = index?.status ?: MutableStateFlow(IndexStatus())

    /** Indexes the open sources [targets]. Call off the UI thread. */
    suspend fun index(targets: List<IndexTarget>) {
        index?.update(targets)
    }

    /** The name at [line], [column] of [document]: the use or definition there, or null. Call off the UI thread. */
    suspend fun nameAt(document: DocumentRef, line: Int, column: Int): NameSpan? {
        val file = index?.file(RepoName(document.repo), document.revision, document.path) ?: return null
        file.facts.referenceAt(line, column)?.let { return NameSpan(it.line, it.column, it.column + (it.end - it.start)) }
        file.facts.definitionAt(line, column)?.let { return NameSpan(it.line, it.column, it.column + (it.nameEnd - it.nameStart)) }
        return null
    }
}
