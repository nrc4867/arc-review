package dev.chieppa.arc.ui.panes

import androidx.compose.runtime.Composable
import dev.chieppa.arc.core.mark.FileReviewState
import dev.chieppa.arc.core.pane.Pane
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.action_mark_and_advance
import dev.chieppa.arc.design.generated.pane_mark_reviewed
import dev.chieppa.arc.ui.actions.ArcActionIds
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.arc.ui.workbench.keysFor
import dev.chieppa.stretch.theme.StretchIcons
import dev.chieppa.stretch.theme.StretchSizes
import dev.chieppa.stretch.ui.ToggleButton
import dev.chieppa.stretch.ui.Tooltip
import org.jetbrains.compose.resources.stringResource

/**
 * The pane header's Mark reviewed button, with its key in its tooltip. It stays on while the file is reviewed; turning
 * it on marks the file and opens the next one to read, turning it off clears the mark.
 */
@Composable
fun MarkReviewedButton(pane: Pane, workbench: Workbench) {
    val source = pane.source ?: return
    val reviewed = workbench.state.states(source)[pane.path] == FileReviewState.Reviewed
    Tooltip(stringResource(Res.string.action_mark_and_advance), workbench.keysFor(ArcActionIds.MARK_AND_ADVANCE)) {
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
    }
}
