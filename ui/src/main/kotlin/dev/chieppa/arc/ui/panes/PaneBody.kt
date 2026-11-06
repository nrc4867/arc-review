package dev.chieppa.arc.ui.panes

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import dev.chieppa.arc.core.pane.Pane
import dev.chieppa.arc.core.pane.PaneView
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.pane_binary
import dev.chieppa.arc.ui.find.FindState
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchSpacing
import org.jetbrains.compose.resources.stringResource

/** The pane's file as its view asks: the change, the whole file, or rendered Markdown. A binary file shows its kind. */
@Composable
fun PaneBody(pane: Pane, text: PaneText, workbench: Workbench, find: FindState) {
    val modifier = Modifier.fillMaxSize()
    if (text.binary) {
        BasicText(stringResource(Res.string.pane_binary), Modifier.padding(StretchSpacing.space16), style = Stretch.type.body.merge(TextStyle(color = Stretch.colors.inkMuted)))
        return
    }
    val whole = text.new ?: text.old.orEmpty()
    when {
        pane.view == PaneView.Rendered -> MarkdownBody(whole, modifier)
        pane.view == PaneView.Diff && text.diff != null -> DiffBody(pane, text.diff, workbench, find, modifier)
        else -> FullFileBody(pane, whole, text.diff, workbench, find, modifier, removed = text.new == null)
    }
}
