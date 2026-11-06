package dev.chieppa.arc.ui.panes

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.chieppa.arc.core.pane.Pane
import dev.chieppa.arc.core.pane.PaneView
import dev.chieppa.arc.ui.find.FindState
import dev.chieppa.arc.ui.workbench.Workbench

/**
 * The pane's file as [view] says: the change, the whole file, or rendered Markdown, filling the pane. A file of [kind]
 * with no changed lines shows whole, a deleted one as it was with every line removed. A binary file shows its kind.
 */
@Composable
fun PaneBody(pane: Pane, text: PaneText, kind: FileKind, view: PaneView, workbench: Workbench, find: FindState) {
    val modifier = Modifier.fillMaxSize()
    if (text.binary) {
        BinaryBody(modifier)
        return
    }
    val whole = text.new ?: text.old.orEmpty()
    when {
        view == PaneView.Rendered -> MarkdownBody(whole, modifier)
        view == PaneView.Diff && kind.hasDiff && text.diff != null -> DiffBody(pane, text.diff, workbench, find, modifier)
        else -> FullFileBody(pane, whole, text.diff, workbench, find, modifier, removed = kind == FileKind.Deleted)
    }
}
