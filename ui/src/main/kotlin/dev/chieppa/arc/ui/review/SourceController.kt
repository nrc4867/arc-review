package dev.chieppa.arc.ui.review

import dev.chieppa.arc.core.change.ObjectId
import dev.chieppa.arc.core.mark.ReplayOutcome
import dev.chieppa.arc.core.pane.Page
import dev.chieppa.arc.core.revision.RecordedHead
import dev.chieppa.arc.core.source.RepoName
import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.core.source.SourceRefs
import dev.chieppa.arc.ui.services.ReviewData
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.nio.file.Path

/**
 * The workspace's repos and sources: adding them, switching the active one, scoping its tree to one commit, and reading
 * each from its repo in the background. A new read of a source cancels the one before it; the old view stays until
 * the new one is ready.
 */
class SourceController(
    private val state: WorkspaceState,
    private val data: ReviewData,
    private val scope: CoroutineScope,
    private val now: () -> Long = { System.currentTimeMillis() / MILLIS_PER_SECOND },
) {
    private val jobs = mutableMapOf<Pair<Source, Boolean>, Job>()

    /** Adds the repo in [dir] under its folder's name, made unique; null when [dir] holds no repo. */
    suspend fun addRepo(dir: Path): RepoName? {
        val folder = dir.toAbsolutePath().normalize()
        state.repos.entries.firstOrNull { it.value == folder }?.let { return it.key }
        if (!data.isRepo(folder)) return null
        val base = RepoNames.fromFolder(folder.fileName?.toString().orEmpty())
        val name = RepoNames.unique(base, state.repos.keys)
        state.repos = state.repos + (name to folder)
        return name
    }

    /** Adds the source [ref] names, if its repo is known, and makes it active. Returns whether it did. A pull request is read through GitHub. */
    fun addSource(ref: String): Boolean {
        val source = SourceRefs.parse(ref) ?: return false
        if (source.repo !in state.repos) return false
        state.workspace = state.workspace.add(source)
        activate(source)
        load(source)
        return true
    }

    /**
     * Takes [source] out of the strip with its view and its unpinned panes. Its marks stay in the workspace, so adding
     * it again brings them back; pinned panes stay, in the next source's layout or the loose one.
     */
    private fun Page.belongsTo(source: Source) = when (this) {
        is Page.RevisionMatrix -> this.source == source
        is Page.Conversation -> this.source == source
        is Page.Filter -> false
    }

    fun removeSource(source: Source) {
        if (source !in state.workspace.sources) return
        jobs.keys.filter { it.first == source }.forEach { jobs.remove(it)?.cancel() }
        val shown = state.viewOf(source).layout.tabs.toSet()
        val closing = state.workspace.panes.filter { !it.pinned && (it.source == source || it.id.value in shown) }.map { it.id }
        val pages = state.workspace.pages.filter { it.belongsTo(source) && !it.pinned }.map { it.id }
        state.workspace = (closing.fold(state.workspace.remove(source)) { workspace, id -> workspace.withoutPane(id) }).let { workspace ->
            pages.fold(workspace) { w, id -> w.withoutPage(id) }
        }
        val ids = closing.map { it.value }.toSet()
        state.paneStates = state.paneStates - ids
        ids.forEach(state.paneLines::remove)
        state.views = state.views - source
        state.loads = state.loads - source
        state.scoped = state.scoped - source
        state.pulls = state.pulls - source
        state.revisions = state.revisions - source
        val next = state.workspace.active
        if (next != null) {
            activate(next)
        } else {
            val pinned = state.workspace.pinnedIds
            state.looseLayout = SourceSwitching.withPinned(state.looseLayout, pinned)
        }
    }

    fun activate(source: Source) {
        val (workspace, views) = SourceSwitching.activate(state.workspace, state.views, source)
        state.workspace = workspace
        state.views = views
    }

    /** Scopes [source]'s tree to [commit], or back to the whole source with null. */
    fun setScope(source: Source, commit: ObjectId?) {
        state.views = state.views + (source to state.viewOf(source).copy(commit = commit))
        if (commit == null) state.scoped = state.scoped - source else loadScope(source)
    }

    /** Reads every source not read yet, and the scoped commit of each that has one. */
    fun loadAll() {
        for (source in state.workspace.sources) {
            if (state.loads[source] == null) load(source)
            if (state.viewOf(source).commit != null && state.scoped[source] == null) loadScope(source)
        }
    }

    /** Reads [source]'s whole change again, with the replays its marks need, and records the head it was read at. */
    fun load(source: Source) = launch(source, scoped = false) {
        val snapshot = data.read(source)
        state.workspace = state.workspace.record(source, RecordedHead(snapshot.revisions.head, snapshot.revisions.base, now()))
        (source as? Source.PullRequest)?.let(data::pullRequest)?.let { state.pulls = state.pulls + (source to it) }
        SourceLoad.Ready(snapshot, replays(source, snapshot))
    }

    private fun loadScope(source: Source) {
        val commit = state.viewOf(source).commit ?: return
        launch(source, scoped = true) { SourceLoad.Ready(data.readCommit(source, commit)) }
    }

    private suspend fun replays(source: Source, snapshot: dev.chieppa.arc.core.change.SourceSnapshot): List<ReplayOutcome> =
        state.workspace.marksIn(source).values.mapNotNull { it.reviewed }.distinct()
            .filter { it.head != snapshot.revisions.head || it.mergeBase != snapshot.revisions.base }
            .mapNotNull { data.replay(source.repo, it, snapshot.revisions) }

    private fun launch(source: Source, scoped: Boolean, read: suspend () -> SourceLoad) {
        val key = source to scoped
        jobs[key]?.cancel()
        fun put(load: SourceLoad) {
            if (scoped) state.scoped = state.scoped + (source to load) else state.loads = state.loads + (source to load)
        }
        val current = if (scoped) state.scoped[source] else state.loads[source]
        if (current == null) put(SourceLoad.Loading)
        jobs[key] = scope.launch {
            val result = try {
                read()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                SourceLoad.Failed(e.message.orEmpty())
            }
            if (source in state.workspace.sources) put(result)
        }
    }
}

private const val MILLIS_PER_SECOND = 1000L
