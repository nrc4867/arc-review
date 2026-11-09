package dev.chieppa.arc.ui.workbench

import dev.chieppa.arc.core.actions.WorkflowRun
import dev.chieppa.arc.core.pane.Page
import dev.chieppa.arc.core.pane.Pane
import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.core.start.StartAction
import dev.chieppa.arc.ui.tree.FileTreeItem
import dev.chieppa.arc.ui.tree.ReviewTree
import dev.chieppa.arc.ui.tree.TreeKeys

/**
 * What an action acts on: the open context menu's target or the start screen's arguments while there is one, and
 * otherwise what the palette means, the tree's selection while the tree has the keyboard or the focused pane and the
 * active source.
 */
object ActionTargets {
    /** The source: the chip's or tree row's, else the active one. */
    fun source(workbench: Workbench): Source? = when (val target = workbench.menuTarget) {
        is MenuTarget.OfSource -> target.source
        is MenuTarget.OfRows -> target.item.source
        is MenuTarget.OfStart -> StartTargets.source(workbench, target.action) ?: workbench.state.active
        else -> workbench.state.active
    }

    /** The one changed file: the tree row's or the tab's, else the tree's one selected file or the file being read. */
    fun file(workbench: Workbench): Pair<Source, String>? = when (val target = workbench.menuTarget) {
        is MenuTarget.OfRows -> (target.item as? FileTreeItem.FileRow)?.let { it.source to it.file.path }
        is MenuTarget.OfPane -> workbench.state.pane(target.id)?.let(::changeOf)
        is MenuTarget.OfStart -> StartTargets.file(workbench, target.action)
        is MenuTarget.OfSource, is MenuTarget.OfPull, is MenuTarget.OfRun -> null
        null -> selectedFile(workbench) ?: workbench.marks.current
    }

    /** The files to mark: the tree rows' selection, the tab's file, else the marking keys' targets. */
    fun files(workbench: Workbench): Map<Source, List<String>> = when (val target = workbench.menuTarget) {
        is MenuTarget.OfRows -> ReviewTargets.selected(ReviewTree.roots(workbench.state), target.selection + target.item.key)
        is MenuTarget.OfPane -> workbench.state.pane(target.id)?.let(::changeOf)?.let { (source, path) -> mapOf(source to listOf(path)) }.orEmpty()
        is MenuTarget.OfStart -> StartTargets.file(workbench, target.action)?.let { (source, path) -> mapOf(source to listOf(path)) }.orEmpty()
        is MenuTarget.OfSource, is MenuTarget.OfPull, is MenuTarget.OfRun -> emptyMap()
        null -> ReviewTargets.of(workbench)
    }

    /** The pane: the tab's, else the focused one. */
    fun pane(workbench: Workbench): Pane? =
        (workbench.menuTarget as? MenuTarget.OfPane)?.let { workbench.state.pane(it.id) } ?: workbench.panes.focused

    /** The start screen's click or menu item being run, if it is one. */
    fun start(workbench: Workbench): StartAction? = (workbench.menuTarget as? MenuTarget.OfStart)?.action

    /** The pull request row the menu is open on, if it is one. */
    fun pull(workbench: Workbench): MenuTarget.OfPull? = workbench.menuTarget as? MenuTarget.OfPull

    /** The run the menu is open on, else the run whose page is focused. */
    fun run(workbench: Workbench): WorkflowRun? = (workbench.menuTarget as? MenuTarget.OfRun)?.run
        ?: (workbench.state.paneLayout.activeTab?.let(workbench.state::page) as? Page.Run)?.let { workbench.runs.watch.run(it.run) }

    /** The folder row the menu is open on, if it is one. */
    fun folder(workbench: Workbench): FileTreeItem.FolderRow? =
        (workbench.menuTarget as? MenuTarget.OfRows)?.item as? FileTreeItem.FolderRow

    private fun changeOf(pane: Pane): Pair<Source, String>? = pane.source?.takeIf { pane.hasChange }?.let { it to pane.path }

    private fun selectedFile(workbench: Workbench): Pair<Source, String>? {
        if (!workbench.treeFocused) return null
        val (ref, path) = workbench.treeState.selection.singleOrNull()?.let(TreeKeys::fileOf) ?: return null
        val source = workbench.state.workspace.sources.firstOrNull { it.ref == ref } ?: return null
        return source to path
    }
}
