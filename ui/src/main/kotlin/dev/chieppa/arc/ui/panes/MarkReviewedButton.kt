package dev.chieppa.arc.ui.panes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import dev.chieppa.arc.core.mark.FileReviewState
import dev.chieppa.arc.core.pane.Pane
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.pane_mark_reviewed
import dev.chieppa.arc.ui.actions.ArcKeys
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.frame.caps
import dev.chieppa.stretch.theme.StretchIcons
import dev.chieppa.stretch.theme.StretchSpacing
import dev.chieppa.stretch.ui.Button
import dev.chieppa.stretch.ui.ButtonKind
import dev.chieppa.stretch.ui.Kbd
import org.jetbrains.compose.resources.stringResource

/**
 * The file header's button that marks the pane's file reviewed and opens the next one to read, with its key. Once the
 * file is reviewed it shows on.
 */
@Composable
fun MarkReviewedButton(pane: Pane, workbench: Workbench) {
    val source = pane.source ?: return
    val reviewed = workbench.state.states(source)[pane.path] == FileReviewState.Reviewed
    Row(horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space4), verticalAlignment = Alignment.CenterVertically) {
        Button(
            stringResource(Res.string.pane_mark_reviewed),
            {
                workbench.paneLayout = workbench.paneLayout.activate(pane.id.value)
                workbench.marks.markAndAdvance()
            },
            kind = if (reviewed) ButtonKind.Default else ButtonKind.Quiet,
            icon = StretchIcons.Check,
        )
        Kbd(ArcKeys.MARK_AND_ADVANCE.caps())
    }
}
