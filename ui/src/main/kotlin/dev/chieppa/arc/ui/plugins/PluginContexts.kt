package dev.chieppa.arc.ui.plugins

import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.plugins.protocol.CallContext
import dev.chieppa.arc.plugins.protocol.FileInfo
import dev.chieppa.arc.plugins.protocol.RepoInfo
import dev.chieppa.arc.plugins.protocol.SourceInfo
import dev.chieppa.arc.ui.workbench.Workbench

/** What a call into a plugin acts on, read from the window: a source as plugins see it, and the focused file. */
object PluginContexts {
    private const val GITHUB = "github.com/"

    /** The active source and the file of the focused pane. */
    fun focused(workbench: Workbench): CallContext {
        val state = workbench.state
        val pane = state.paneLayout.activeTab?.let(state::pane)
        val source = pane?.source ?: state.active
        return CallContext(source?.let { info(workbench, it) }, pane?.takeIf { it.source == null || it.source == source }?.let { FileInfo(it.path) })
    }

    /** [source] with no file in focus, as a view opened for it gets. */
    fun of(workbench: Workbench, source: Source?): CallContext = CallContext(source?.let { info(workbench, it) })

    /** [source] as plugins see it: its repo by its GitHub name when it has one, its clone and the head it is read at. */
    fun info(workbench: Workbench, source: Source): SourceInfo {
        val github = workbench.gitHubRepos[source.repo]
        val repo = RepoInfo(github?.let { GITHUB + it.fullName } ?: source.repo.value, source.repo.value)
        val head = workbench.state.loads[source]?.ready?.revisions?.head?.value
        return SourceInfo(source.ref, kindOf(source), repo, workbench.state.repos[source.repo]?.toString(), head)
    }

    private fun kindOf(source: Source): String = when (source) {
        is Source.PullRequest -> "pull"
        is Source.Commit -> "commit"
        is Source.CommitRange -> "range"
        is Source.BranchAgainstBase -> "branch"
    }
}
