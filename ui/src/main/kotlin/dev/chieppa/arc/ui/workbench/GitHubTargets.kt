package dev.chieppa.arc.ui.workbench

import dev.chieppa.arc.core.change.ChangeStatus
import dev.chieppa.arc.core.pane.Pane
import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.host.github.GitHubPages

/**
 * Where on github.com what the window shows is: a source's page, and a file of its change or at a revision. Null for a
 * repo not cloned from GitHub, or a file not read yet.
 */
object GitHubTargets {
    /** [source]'s page, from its repo's GitHub origin, else the address GitHub gave for the pull request. */
    fun source(workbench: Workbench, source: Source): String? {
        workbench.gitHubRepos[source.repo]?.let { return GitHubPages.source(it, source) }
        return workbench.state.pulls[source]?.url
    }

    /**
     * [path] of [source]'s change at [line] when given: in a pull request's Files view, else as it is at the source's head,
     * or at its base for a deleted file.
     */
    fun file(workbench: Workbench, source: Source, path: String, line: Int? = null): String? {
        val repo = workbench.gitHubRepos[source.repo] ?: return null
        val snapshot = workbench.state.snapshot(source)
        val file = snapshot?.file(path)
        val deleted = file?.status == ChangeStatus.Deleted
        if (source is Source.PullRequest) return GitHubPages.pullFile(repo, source.number, path, line, old = deleted)
        if (snapshot == null || file == null) return null
        val revision = if (deleted) snapshot.revisions.base else snapshot.revisions.head
        return GitHubPages.blob(repo, revision.value, if (deleted) file.oldPath ?: path else path, line)
    }

    /** The file [pane] shows at [line] when given: its change's, else the file at the pane's revision. */
    fun pane(workbench: Workbench, pane: Pane, line: Int? = null): String? {
        val source = pane.source
        if (source != null && pane.hasChange) return file(workbench, source, pane.path, line)
        val repo = workbench.gitHubRepos[pane.repo] ?: return null
        return GitHubPages.blob(repo, pane.revision.value, pane.path, line)
    }

    /** The line at the top of [pane], when it is scrolled past the first. */
    fun lineOf(workbench: Workbench, pane: Pane): Int? = workbench.state.paneLines[pane.id.value]?.takeIf { it > 1 }
}
