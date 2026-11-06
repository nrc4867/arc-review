package dev.chieppa.arc.ui.panes

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.onPreviewKeyEvent
import dev.chieppa.arc.core.pane.Pane
import dev.chieppa.arc.core.pane.PaneView
import dev.chieppa.arc.ui.actions.ArcKeys
import dev.chieppa.arc.ui.find.ArcFindBar
import dev.chieppa.arc.ui.find.FindState
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.frame.chord
import dev.chieppa.stretch.theme.StretchSpacing
import dev.chieppa.stretch.ui.Divider

/**
 * One pane: its tool row over its file, shown as the pane's view asks once its text is read, with the find bar
 * floating at the top right while it is open. A file with no changed lines, added or deleted, shows whole, filling the
 * pane. Ctrl F in the pane opens find. Until the text arrives the pane is plain.
 */
@Composable
fun PaneContent(pane: Pane, workbench: Workbench, modifier: Modifier = Modifier) {
    val find = remember(pane.id) { FindState() }
    Column(
        modifier.fillMaxSize().onPreviewKeyEvent { event ->
            if (event.chord() != ArcKeys.FIND_IN_FILE) return@onPreviewKeyEvent false
            find.show()
            true
        },
    ) {
        key(pane.id, pane.path, pane.revision, pane.base) {
            val text = produceState<PaneText?>(null, pane) { value = workbench.texts.load(pane) }.value
            val kind = text?.let { FileKind.of(pane, it) }
            PaneToolRow(pane, kind, text?.binary == true, workbench, find)
            Divider()
            Box(Modifier.weight(1f).fillMaxWidth()) {
                if (text != null && kind != null) {
                    val view = PaneViews.shown(pane, kind, text.binary)
                    PaneBody(pane, text, kind, view, workbench, find)
                    if (find.open && view != PaneView.Rendered && !text.binary) {
                        ArcFindBar(find, { step -> workbench.runOnPane(pane.id.value) { moveMatch(step) } }, Modifier.align(Alignment.TopEnd).padding(StretchSpacing.space8))
                    }
                }
            }
        }
    }
}
