package dev.chieppa.arc.ui.panes

import dev.chieppa.arc.core.pane.Pane
import dev.chieppa.arc.core.pane.PaneView
import dev.chieppa.arc.ui.review.PaneFactory

/**
 * The ways a pane can show its file: the diff only when it has changed lines, the whole file for a change or for
 * Markdown and SVG, and Markdown or SVG rendered. A binary file has none.
 */
object PaneViews {
    fun of(pane: Pane, kind: FileKind, binary: Boolean = false): List<PaneView> = buildList {
        if (binary) return@buildList
        val rendered = PaneFactory.rendersFirst(pane.path)
        if (pane.hasChange) {
            if (kind.hasDiff) add(PaneView.Diff)
            add(PaneView.FullFile)
            if (rendered) add(PaneView.Rendered)
        } else if (rendered) {
            add(PaneView.Rendered)
            add(PaneView.FullFile)
        }
    }

    /** How the pane shows now: its own view where its file offers it, else the whole file. */
    fun shown(pane: Pane, kind: FileKind, binary: Boolean = false): PaneView {
        val views = of(pane, kind, binary)
        return if (pane.view in views || (views.isEmpty() && pane.view != PaneView.Diff)) pane.view else PaneView.FullFile
    }
}
