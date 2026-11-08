package dev.chieppa.arc.ui.review

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dev.chieppa.arc.core.change.SourceSnapshot
import dev.chieppa.arc.core.mark.FileReviewState
import dev.chieppa.arc.core.mark.FileStates
import dev.chieppa.arc.core.mark.ReviewProgress
import dev.chieppa.arc.core.pane.Page
import dev.chieppa.arc.core.pane.Pane
import dev.chieppa.arc.core.workspace.Workspace
import dev.chieppa.arc.core.source.RepoName
import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.host.github.pulls.PullRequestSummary
import dev.chieppa.arc.ui.revisions.RevisionsRead
import dev.chieppa.stretch.frame.PaneLayout
import java.nio.file.Path

/**
 * The workspace as the window shows it: the [workspace] itself, the repos it reads from, each source's view and how
 * far reading it has got, and each pane's own state. Views read it and recompose when it changes.
 */
class WorkspaceState(
    workspace: Workspace = Workspace(),
    repos: Map<RepoName, Path> = emptyMap(),
    views: Map<Source, SourceView> = emptyMap(),
    paneStates: Map<String, PaneUiState> = emptyMap(),
) {
    var workspace by mutableStateOf(workspace)
    var repos by mutableStateOf(repos)
    var views by mutableStateOf(views)
    var paneStates by mutableStateOf(paneStates)

    /**
     * The line at the top of each pane, by pane id. It changes with every scroll, so it is kept outside Compose state
     * and read when the workspace is saved.
     */
    val paneLines: MutableMap<String, Int> = java.util.concurrent.ConcurrentHashMap(paneStates.mapValues { it.value.line })

    /** Each source's whole change, as read. */
    var loads by mutableStateOf<Map<Source, SourceLoad>>(emptyMap())

    /** Each source's change in the one commit its tree is scoped to, as read. */
    var scoped by mutableStateOf<Map<Source, SourceLoad>>(emptyMap())

    /** Each source's revisions as last read, with the checks on their heads. */
    var revisions by mutableStateOf<Map<Source, RevisionsRead>>(emptyMap())

    /** What the host said of each pull request source when it was last read. */
    var pulls by mutableStateOf<Map<Source, PullRequestSummary>>(emptyMap())

    /** The panes shown while the workspace has no source. */
    var looseLayout by mutableStateOf(PaneLayout())

    val active: Source? get() = workspace.active

    /** The sources added to the selection beside the active one with Ctrl+click in the strip. Window state, not saved. */
    var selected by mutableStateOf<Set<Source>>(emptySet())

    /** The selection in strip order: the active source, always, and the sources added beside it. */
    val selection: List<Source> get() = workspace.sources.filter { it == active || it in selected }

    /** Adds [source] to the selection, or takes it out when it is in; the active source always stays in. */
    fun toggleSelected(source: Source) {
        if (source == active || source !in workspace.sources) return
        selected = if (source in selected) selected - source else selected + source
    }

    /** Leaves the active source alone in the selection. */
    fun selectOnlyActive() {
        selected = emptySet()
    }

    /** The active source's pane layout. */
    var paneLayout: PaneLayout
        get() = active?.let { views[it]?.layout } ?: looseLayout
        set(value) {
            val source = active
            if (source == null) looseLayout = value else views = views + (source to viewOf(source).copy(layout = value))
        }

    fun viewOf(source: Source): SourceView = views[source] ?: SourceView()

    fun pane(id: String): Pane? = workspace.panes.firstOrNull { it.id.value == id }

    /** The page with tab [id], when the tab shows no file. */
    fun page(id: String): Page? = workspace.pages.firstOrNull { it.id.value == id }

    fun paneState(id: String): PaneUiState = paneStates[id] ?: PaneUiState()

    fun setPaneState(id: String, state: PaneUiState) {
        paneStates = paneStates + (id to state)
    }

    /** What the tree shows for [source]: its scoped commit's change when it has one, else its whole change. */
    fun shown(source: Source): SourceLoad? = if (viewOf(source).commit != null) scoped[source] else loads[source]

    /** Where each file of [source]'s whole change stands, by path. */
    fun states(source: Source): Map<String, FileReviewState> {
        val ready = loads[source] as? SourceLoad.Ready ?: return emptyMap()
        return FileStates.of(ready.snapshot, workspace.marksIn(source), ready.replays)
    }

    /** How much of [source] is reviewed, or null before it is read. */
    fun progress(source: Source): ReviewProgress? = loads[source]?.ready?.let { ReviewProgress.of(states(source).values) }

    /** The whole change of [source], once read. */
    fun snapshot(source: Source): SourceSnapshot? = loads[source]?.ready
}
