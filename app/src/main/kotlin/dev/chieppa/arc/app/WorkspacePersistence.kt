package dev.chieppa.arc.app

import dev.chieppa.arc.core.change.ObjectId
import dev.chieppa.arc.core.source.RepoName
import dev.chieppa.arc.store.workspace.StoredPaneState
import dev.chieppa.arc.store.workspace.StoredSourceView
import dev.chieppa.arc.store.workspace.StoredWorkspace
import dev.chieppa.arc.store.workspace.WorkspaceMapping
import dev.chieppa.arc.ui.review.PaneUiState
import dev.chieppa.arc.ui.review.SourceView
import dev.chieppa.arc.ui.review.WorkspaceState
import dev.chieppa.arc.ui.tree.TreeKeys
import dev.chieppa.stretch.frame.PaneLayout
import java.nio.file.Path

/** Turns the workspace's state into a stored workspace and back, with each source's view and each pane's state. */
internal object WorkspacePersistence {
    fun toState(stored: StoredWorkspace, repos: Map<RepoName, Path>): WorkspaceState {
        val workspace = WorkspaceMapping.toWorkspace(stored)
        val panes = workspace.tabIds
        val views = stored.views.mapNotNull { view ->
            val source = workspace.sources.firstOrNull { it.ref == view.source } ?: return@mapNotNull null
            val layout = view.layout?.let { LayoutMapping.toLayout(it, panes) }
            val commit = view.commit?.let { runCatching { ObjectId(it) }.getOrNull() }
            source to SourceView(layout ?: PaneLayout(), view.expanded.toSet(), commit)
        }.toMap()
        val paneStates = stored.paneStates.filterKeys { it in panes }.mapValues { (_, s) -> PaneUiState(s.split, s.line, s.wrap, s.docs) }
        return WorkspaceState(workspace, repos, views, paneStates)
    }

    fun toStored(state: WorkspaceState, expanded: Set<Any>): StoredWorkspace {
        val keys = expanded.filterIsInstance<String>()
        val views = state.workspace.sources.map { source ->
            val view = state.viewOf(source)
            StoredSourceView(
                source = source.ref,
                layout = LayoutMapping.toStored(view.layout),
                expanded = keys.filter { TreeKeys.belongsTo(it, source) },
                commit = view.commit?.value,
            )
        }
        val paneStates = state.workspace.panes.associate { pane ->
            val id = pane.id.value
            id to state.paneState(id).let { StoredPaneState(it.split, state.paneLines[id] ?: 0, it.wrap, it.docs) }
        }
        return WorkspaceMapping.toStored(state.workspace, views, paneStates)
    }
}
