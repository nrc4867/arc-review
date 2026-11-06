package dev.chieppa.arc.ui.panes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import dev.chieppa.arc.core.mark.FileReviewState
import dev.chieppa.arc.core.pane.Pane
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.pane_mark_reviewed
import dev.chieppa.arc.ui.actions.ArcActionIds
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.frame.capsFor
import dev.chieppa.stretch.theme.StretchIcons
import dev.chieppa.stretch.theme.StretchSizes
import dev.chieppa.stretch.theme.StretchSpacing
import dev.chieppa.stretch.ui.Kbd
import dev.chieppa.stretch.ui.ToggleButton
import org.jetbrains.compose.resources.stringResource

/**
 * The pane header's Mark reviewed button, with its key: it stays on while the file is reviewed. Turning it on marks
 * the file and opens the next one to read; turning it off clears the mark.
 */
@Composable
fun MarkReviewedButton(pane: Pane, workbench: Workbench) {
    val source = pane.source ?: return
    val reviewed = workbench.state.states(source)[pane.path] == FileReviewState.Reviewed
    val keys = workbench.registry[ArcActionIds.MARK_AND_ADVANCE]?.let(workbench.keymap::capsFor)
    Row(horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space4), verticalAlignment = Alignment.CenterVertically) {
        ToggleButton(
            stringResource(Res.string.pane_mark_reviewed),
            reviewed,
            { on ->
                if (on) {
                    workbench.paneLayout = workbench.paneLayout.activate(pane.id.value)
                    workbench.marks.markAndAdvance()
                } else {
                    workbench.marks.setReviewed(source, listOf(pane.path), false)
                }
            },
            icon = StretchIcons.Check,
            height = StretchSizes.seg,
        )
        keys?.let { Kbd(it) }
    }
}
