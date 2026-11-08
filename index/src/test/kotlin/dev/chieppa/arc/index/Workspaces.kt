package dev.chieppa.arc.index

import dev.chieppa.arc.codeintel.CodeIntel
import dev.chieppa.arc.codeintel.model.CodePosition
import dev.chieppa.arc.codeintel.model.DefinitionRequest
import dev.chieppa.arc.codeintel.model.SourceScope
import dev.chieppa.arc.codeintel.router.CodeIntelRouter
import dev.chieppa.arc.core.source.RepoName
import dev.chieppa.arc.index.nav.Documents
import dev.chieppa.arc.index.nav.TreeSitterNavigation
import kotlinx.coroutines.CoroutineDispatcher

/** An index over a [FakeBlobStore] with the tree-sitter answers registered, and a way to click in it. */
class Workspaces(val store: FakeBlobStore, dispatcher: CoroutineDispatcher, val targets: List<IndexTarget>) {
    val index = WorkspaceIndex(store, dispatcher)
    val intel = CodeIntel()
    val router = CodeIntelRouter(intel)

    init {
        TreeSitterNavigation(index, store).register(intel)
    }

    val sources: List<SourceScope> get() = targets.map { SourceScope(it.source, it.repo.value, it.revision, it.changedPaths.toList()) }

    /** A Ctrl+click on the [occurrence]th [name] (from 0) in [path] of [repo] at [revision]. */
    fun click(repo: String, revision: String, path: String, name: String, occurrence: Int = 0): DefinitionRequest {
        val text = textOf(repo, revision, path)
        var offset = -1
        repeat(occurrence + 1) { offset = Regex("\\b${Regex.escape(name)}\\b").find(text, offset + 1)!!.range.first }
        val before = text.substring(0, offset)
        val position = CodePosition(before.count { it == '\n' } + 1, offset - (before.lastIndexOf('\n') + 1))
        return DefinitionRequest(Documents.of(RepoName(repo), revision, path), position, sources)
    }

    private val texts = HashMap<Triple<String, String, String>, String>()

    fun remember(repo: String, revision: String, files: Map<String, String>) {
        files.forEach { (path, text) -> texts[Triple(repo, revision, path)] = text }
        store.put(repo, revision, files)
    }

    private fun textOf(repo: String, revision: String, path: String) = texts.getValue(Triple(repo, revision, path))
}
