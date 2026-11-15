package dev.chieppa.arc.ui.panes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.withFrameNanos
import dev.chieppa.arc.codeintel.model.DocumentRef
import dev.chieppa.arc.core.conversation.LineSide
import dev.chieppa.arc.core.pane.Pane
import dev.chieppa.arc.ui.conversation.ThreadReveal
import dev.chieppa.arc.ui.navigation.NavigationSizes
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.code.CodeRange
import dev.chieppa.stretch.diff.DiffPosition
import dev.chieppa.stretch.diff.DiffSide
import dev.chieppa.stretch.diff.DiffViewerState

/**
 * Brings the places jumps go to into [viewer], the diff of [pane] whose sides show [documents]: a definition, usage,
 * outline row or history place, and a thread under its line. The fold hiding the line opens, the line comes near the
 * top, and the name, or the whole line, flashes.
 */
@Composable
fun DiffJumps(pane: Pane, viewer: DiffViewerState, documents: Map<DiffSide, DocumentRef>, workbench: Workbench) {
    val navigation = workbench.navigation
    val jump = navigation.reveal?.takeIf { jump -> documents.values.any { it.repo == jump.repo.value && it.revision == jump.revision && it.path == jump.path } }
    LaunchedEffect(viewer, jump) {
        jump ?: return@LaunchedEffect
        withFrameNanos {}
        val onNew = documents.getValue(DiffSide.New).revision == jump.revision && jump.side == LineSide.New
        val side = if (onNew) DiffSide.New else DiffSide.Old
        viewer.revealLine(0, side, jump.line, NavigationSizes.LEAD_IN)
        viewer.caret = DiffPosition(0, side, jump.line, jump.column)
        val end = jump.end
        if (end == null) viewer.flashLine(0, side, jump.line) else viewer.flash(0, side, CodeRange(jump.line, jump.column, end))
        navigation.reveal = null
    }
    val reveal = workbench.threadViews.reveal?.takeIf { it.source == pane.source && it.path == pane.path }
    LaunchedEffect(viewer, reveal) {
        reveal ?: return@LaunchedEffect
        withFrameNanos {}
        val side = if (reveal.side == LineSide.Old) DiffSide.Old else DiffSide.New
        viewer.revealLine(0, side, reveal.line, ThreadReveal.LEAD_IN)
        viewer.flashLine(0, side, reveal.line)
        workbench.threadViews.reveal = null
    }
}
