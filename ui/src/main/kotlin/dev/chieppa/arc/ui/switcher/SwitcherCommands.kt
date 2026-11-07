package dev.chieppa.arc.ui.switcher

import dev.chieppa.arc.core.pane.Revision
import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.ui.workbench.Workbench

/**
 * What picking a switcher row does on [workbench]. A source in the review is switched to; one outside it is added and
 * switched to, or with [SwitcherPick.Add] only added. A file opens in the focused pane, or in a new one with
 * [SwitcherPick.NewPane], which for a source opens its first changed file once it is read.
 */
class SwitcherCommands(private val workbench: Workbench) {
    fun pick(item: SwitcherItem, how: SwitcherPick) {
        workbench.switcher.close()
        when (item) {
            is SwitcherItem.InReview -> goTo(item.source, how)
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
        }
    }

    fun pick(option: OpenAs, how: SwitcherPick) {
        workbench.switcher.close()
        when (option) {
            is OpenAs.NewReview -> workbench.onNewReviewWith(option.source.ref)
            else -> goTo(option.source, how)
        }
    }

    private fun goTo(source: Source, how: SwitcherPick) {
        val held = source in workbench.state.review.sources
        when {
            held -> workbench.activateSource(source)
            how == SwitcherPick.Add -> workbench.addSourceBeside(source.ref)
            else -> workbench.addSource(source.ref)
        }
        if (how == SwitcherPick.NewPane) workbench.openFirstChange(source)
    }
}
