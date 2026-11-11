package dev.chieppa.arc.ui.hints

import dev.chieppa.arc.codeintel.model.SymbolTarget
import dev.chieppa.arc.core.pane.Revision
import dev.chieppa.arc.core.source.RepoName
import dev.chieppa.arc.ui.panes.DiffLines
import dev.chieppa.arc.ui.review.WorkspaceState
import dev.chieppa.arc.ui.services.ReviewData
import dev.chieppa.arc.ui.util.TextDecoding
import dev.chieppa.stretch.diff.diffTexts

/**
 * The lines an open source's change writes in the file a hint points at, so a card can set apart what the change
 * wrote: empty unless the file is one the source changes, read at the source's head. The latest few are kept.
 */
class HintChanges(private val state: WorkspaceState, private val data: ReviewData) {
    private val recent = object : LinkedHashMap<Key, Set<Int>>(HintSizes.KEPT_CHANGES, LOAD_FACTOR, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Key, Set<Int>>) = size > HintSizes.KEPT_CHANGES
    }

    private data class Key(val repo: String, val base: String, val head: String, val path: String)

    /** The new-side line numbers [target]'s file adds in its source's change. Call off the UI thread. */
    suspend fun added(target: SymbolTarget): Set<Int> {
        val document = target.location.document
        val source = state.workspace.sources.firstOrNull { it.ref == target.source } ?: return emptySet()
        val snapshot = state.snapshot(source) ?: return emptySet()
        if (snapshot.revisions.head.value != document.revision) return emptySet()
        val changed = snapshot.file(document.path) ?: return emptySet()
        val key = Key(document.repo, snapshot.revisions.base.value, document.revision, document.path)
        synchronized(recent) { recent[key] }?.let { return it }
        val repo = RepoName(document.repo)
        val old = changed.oldPath?.let { data.readFile(repo, Revision(key.base), it) }?.let(TextDecoding::decode)
        val new = data.readFile(repo, Revision(key.head), document.path)?.let(TextDecoding::decode) ?: return emptySet()
        val added = DiffLines.added(diffTexts(old.orEmpty(), new, changed.oldPath, document.path)).toSet()
        synchronized(recent) { recent[key] = added }
        return added
    }

    private companion object {
        const val LOAD_FACTOR = 0.75f
    }
}
