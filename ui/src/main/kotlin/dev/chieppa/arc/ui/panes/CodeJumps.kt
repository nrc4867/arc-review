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
import dev.chieppa.stretch.code.CodePosition
import dev.chieppa.stretch.code.CodeRange
import dev.chieppa.stretch.code.CodeViewerState

/**
 * Brings the places jumps go to into [viewer], the whole file of [pane] at [document]: a definition, usage, outline row
 * or history place, and a thread under its line. The line comes near the top and the name, or the whole line, flashes.
 */
@Composable
fun CodeJumps(pane: Pane, viewer: CodeViewerState, document: DocumentRef, workbench: Workbench) {
    val navigation = workbench.navigation
    val jump = navigation.reveal?.takeIf { it.repo == pane.repo && it.revision == document.revision && it.path == document.path }
    LaunchedEffect(viewer, jump) {
        jump ?: return@LaunchedEffect
        withFrameNanos {}
        viewer.scrollToLine(jump.line, NavigationSizes.LEAD_IN)
        viewer.caret = CodePosition(jump.line, jump.column)
        val end = jump.end
        if (end == null) viewer.flashLine(jump.line) else viewer.flash(CodeRange(jump.line, jump.column, end))
        navigation.reveal = null
    }
    val reveal = workbench.threadViews.reveal?.takeIf { it.source == pane.source && it.path == pane.path && it.side == LineSide.New }
    LaunchedEffect(viewer, reveal) {
        reveal ?: return@LaunchedEffect
        withFrameNanos {}
        viewer.scrollToLine(reveal.line, ThreadReveal.LEAD_IN)
        viewer.flashLine(reveal.line)
        workbench.threadViews.reveal = null
    }
}
