package dev.chieppa.arc.ui.switcher

import dev.chieppa.arc.core.pane.Revision
import dev.chieppa.arc.core.source.RepoName
import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.ui.workbench.Workbench
import kotlinx.coroutines.launch
import java.nio.file.Path

/**
 * What picking a switcher row does on [workbench]. A source in the strip is switched to; one outside it is opened in the
 * strip and switched to, or with [SwitcherPick.Beside] opened without switching. A file opens in the focused pane, or in a new one with
 * [SwitcherPick.NewPane], which for a source opens its first changed file once it is read. A pasted folder is followed as a repo.
 * Picking runs [close] first; the switcher's own by default, the start screen's field passes its own.
 */
class SwitcherCommands(
    private val workbench: Workbench,
    private val close: () -> Unit = { workbench.switcher.close() },
    private val onRepoAdded: (RepoName) -> Unit = { workbench.switcher.show(SwitcherScope.Sources, it.value) },
) {
    fun pick(item: SwitcherItem, how: SwitcherPick) {
        if (item is SwitcherItem.GitHubRepository) return follow(item)
        close()
        when (item) {
            is SwitcherItem.InWorkspace -> goTo(item.source, how)
            is SwitcherItem.Changed -> {
                workbench.activateSource(item.source)
                workbench.panes.openChange(item.source, item.file.path, newTab = how == SwitcherPick.NewPane)
            }
            is SwitcherItem.AtHead -> {
                val head = workbench.state.snapshot(item.source)?.revisions?.head ?: return
                workbench.activateSource(item.source)
                workbench.panes.openFile(item.source.repo, Revision(head.value), item.path, item.source)
            }
            is SwitcherItem.FollowedPull -> goTo(item.source, how)
            is SwitcherItem.FollowedBranch -> goTo(item.source, how)
            is SwitcherItem.Recent -> goTo(item.source, how)
            is SwitcherItem.FollowedCommit -> goTo(item.source, how)
            is SwitcherItem.GitHubRepository -> Unit
        }
    }

    fun pick(option: OpenAs, how: SwitcherPick) {
        close()
        goTo(option.source, how)
    }

    /** Follows the repo in [folder] and hands it to [onRepoAdded], or calls [onFailed] when the folder holds no repo. */
    fun addRepo(folder: Path, onFailed: () -> Unit) {
        workbench.scope.launch {
            val name = workbench.sources.addRepo(folder)
            if (name == null) onFailed() else onRepoAdded(name)
        }
    }

    /** Searches the followed repo [item] names, or clones it first, keeping the field open while it clones. */
    private fun follow(item: SwitcherItem.GitHubRepository) {
        item.local?.let { return onRepoAdded(it) }
        workbench.followGitHub(item.repo, onRepoAdded)
    }

    private fun goTo(source: Source, how: SwitcherPick) {
        val held = source in workbench.state.workspace.sources
        when {
            held -> workbench.activateSource(source)
            how == SwitcherPick.Beside -> workbench.addSourceBeside(source.ref)
            else -> workbench.addSource(source.ref)
        }
        if (how == SwitcherPick.NewPane) workbench.openFirstChange(source)
    }
}
