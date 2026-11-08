package dev.chieppa.arc.ui.navigation

import dev.chieppa.arc.codeintel.model.DocumentRef
import dev.chieppa.arc.codeintel.model.SourceScope
import dev.chieppa.arc.core.conversation.LineSide
import dev.chieppa.arc.core.pane.Pane
import dev.chieppa.arc.index.IndexTarget
import dev.chieppa.arc.index.nav.Documents
import dev.chieppa.arc.ui.review.WorkspaceState

/** What the index and the providers are told of the workspace: each read source at its head, and a pane's file. */
object NavigationTargets {
    /** Every source read so far, at the head it was read at, with the paths its change touches. */
    fun of(state: WorkspaceState): List<IndexTarget> = state.workspace.sources.mapNotNull { source ->
        state.snapshot(source)?.let { snapshot -> IndexTarget(source.ref, source.repo, snapshot.revisions.head.value, snapshot.files.map { it.path }.toSet()) }
    }

    fun scopes(targets: List<IndexTarget>): List<SourceScope> =
        targets.map { SourceScope(it.source, it.repo.value, it.revision, it.changedPaths.sorted()) }

    /** The file [pane] shows on [side]: at its revision, or at its base for the old side. */
    fun document(pane: Pane, side: LineSide): DocumentRef {
        val base = pane.base
        return if (side == LineSide.Old && base != null) {
            Documents.of(pane.repo, base.value, pane.oldPath ?: pane.path)
        } else {
            Documents.of(pane.repo, pane.revision.value, pane.path)
        }
    }
}
