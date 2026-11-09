package dev.chieppa.arc.ui.switcher

import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import dev.chieppa.arc.core.pane.Revision
import dev.chieppa.arc.core.source.CommitSha
import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.host.github.pulls.PullState
import dev.chieppa.arc.ui.workbench.Workbench
import kotlinx.coroutines.delay

/**
 * What the switcher searches for [query] in [scope], kept current: the strip's sources and their changed files, the
 * active source's other files at its head, and while sources are searched, what followed repos hold: their latest
 * updated open pull requests and those the saved filters found, to suggest from and match fuzzily as soon as it opens;
 * once typing pauses, those GitHub finds by title; the clones' local branches; the sources opened lately; and once
 * something is typed, each clone's newest commits and any commit or range a typed sha names. Without
 * a token only the branches and recent sources come back.
 */
@Composable
fun rememberSwitcherInputs(workbench: Workbench, query: String, scope: SwitcherScope): SwitcherInputs {
    val state = workbench.state
    val active = state.active
    val head = active?.let(state::snapshot)?.revisions?.head
    val headPaths = produceState(emptyList<String>(), active, head) {
        value = if (active == null || head == null) emptyList() else workbench.data.listFiles(active.repo, Revision(head.value))
    }.value
    val words = query.trim()
    val searchesFollowed = scope != SwitcherScope.Files
    val repos = state.repos.keys
    val gitHubRepos = workbench.gitHubRepos.values.toSet()
    val branches = produceState(emptyMap(), searchesFollowed, repos) {
        value = if (searchesFollowed) repos.associateWith { workbench.followed.branches(it) } else emptyMap()
    }.value
    val latest = produceState(emptyList(), searchesFollowed, gitHubRepos) {
        value = if (searchesFollowed) workbench.followed.pulls(gitHubRepos, "").orEmpty() else emptyList()
    }.value
    val byTitle = produceState(emptyList(), words, searchesFollowed, gitHubRepos) {
        if (!searchesFollowed || words.isEmpty()) {
            value = emptyList()
            return@produceState
        }
        delay(SwitcherLimits.DEBOUNCE_MILLIS)
        value = workbench.followed.pulls(gitHubRepos, words).orEmpty()
    }.value
    val commits = produceState(emptyList<SwitcherItem.FollowedCommit>(), words, searchesFollowed, repos) {
        value = emptyList()
        if (!searchesFollowed || words.isEmpty()) return@produceState
        val named = CommitQuery.read(words)?.let { workbench.followed.named(it, repos) }.orEmpty()
        val recent = repos.flatMap { repo -> workbench.followed.commits(repo).map { SwitcherItem.FollowedCommit(Source.Commit(repo, CommitSha(it.id.value)), it) } }
        value = named + recent
    }.value
    val filtered = workbench.watch.results.values.flatMap { it.pulls }.filter { it.state == PullState.Open }
    val sources = state.workspace.sources
    val changed = sources.flatMap { source -> state.snapshot(source)?.files.orEmpty().map { SwitcherItem.Changed(source, it) } }
    val activeChanged = active?.let(state::snapshot)?.files.orEmpty().map { it.path }.toSet()
    return SwitcherInputs(
        sources = sources.map { SwitcherItem.InWorkspace(it, state.pulls[it]?.title ?: state.snapshot(it)?.title.orEmpty()) },
        files = changed,
        headFiles = if (active == null) emptyList() else headPaths.filter { it !in activeChanged }.map { SwitcherItem.AtHead(active, it) },
        followed = if (!searchesFollowed) {
            emptyList()
        } else {
            FollowedResults.of(
                pulls = latest + filtered + byTitle,
                branches = branches,
                gitHubRepos = workbench.gitHubRepos,
                inWorkspace = sources,
                recent = workbench.start.recent.sources,
                viewer = workbench.watch.viewer,
                repos = repos,
                commits = commits,
            )
        },
    )
}
