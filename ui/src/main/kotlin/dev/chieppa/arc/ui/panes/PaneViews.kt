package dev.chieppa.arc.ui.panes

import dev.chieppa.arc.core.pane.Pane
import dev.chieppa.arc.core.pane.PaneView
import dev.chieppa.arc.ui.review.PaneFactory

/** The ways a pane can show its file: its change and the whole file when it has one, rendered for Markdown. */
object PaneViews {
    fun of(pane: Pane): List<PaneView> = buildList {
        val markdown = PaneFactory.isMarkdown(pane.path)
        if (pane.hasChange) {
            add(PaneView.Diff)
            add(PaneView.FullFile)
            if (markdown) add(PaneView.Rendered)
        } else if (markdown) {
            add(PaneView.Rendered)
            add(PaneView.FullFile)
        }
    }
}
