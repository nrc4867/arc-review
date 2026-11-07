package dev.chieppa.arc.ui.switcher

import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import dev.chieppa.arc.core.pane.Revision
import dev.chieppa.arc.ui.workbench.Workbench
import kotlinx.coroutines.delay

/**
 * What the switcher searches for [query] in [scope], kept current: the strip's sources and their changed files, the
 * active source's other files at its head, and once typing pauses, the open pull requests GitHub finds in followed
 * repos and their local branches. Without a token only the branches come back.
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
    val searchesFollowed = scope != SwitcherScope.Files && words.isNotEmpty()
    val repos = state.repos.keys
    val branches = produceState(emptyMap(), searchesFollowed, repos) {
        value = if (searchesFollowed) repos.associateWith { workbench.followed.branches(it) } else emptyMap()
    }.value
    val pulls = produceState(emptyList(), words, searchesFollowed, workbench.gitHubRepos) {
        if (!searchesFollowed) {
            value = emptyList()
            return@produceState
        }
        delay(SwitcherLimits.DEBOUNCE_MILLIS)
        value = workbench.followed.pulls(workbench.gitHubRepos.values.toSet(), words).orEmpty()
    }.value
    val sources = state.workspace.sources
    val changed = sources.flatMap { source -> state.snapshot(source)?.files.orEmpty().map { SwitcherItem.Changed(source, it) } }
    val activeChanged = active?.let(state::snapshot)?.files.orEmpty().map { it.path }.toSet()
    return SwitcherInputs(
        sources = sources.map { SwitcherItem.InWorkspace(it, state.pulls[it]?.title ?: state.snapshot(it)?.title.orEmpty()) },
        files = changed,
        headFiles = if (active == null) emptyList() else headPaths.filter { it !in activeChanged }.map { SwitcherItem.AtHead(active, it) },
        followed = FollowedResults.of(words, pulls, branches, workbench.gitHubRepos, sources),
    )
}
