package dev.chieppa.arc.ui.panes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import dev.chieppa.arc.core.conversation.LineSide
import dev.chieppa.arc.core.pane.Pane
import dev.chieppa.arc.ui.conversation.PaneComments
import dev.chieppa.arc.ui.conversation.PaneThread
import dev.chieppa.arc.ui.conversation.paneThreads
import dev.chieppa.arc.ui.find.FindState
import dev.chieppa.arc.ui.find.ViewerMatches
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.diff.DiffMark
import dev.chieppa.stretch.diff.DiffSide
import dev.chieppa.stretch.diff.DiffSlots
import dev.chieppa.stretch.diff.DiffViewer
import dev.chieppa.stretch.diff.DiffViewerState
import dev.chieppa.stretch.diff.FileDiff
import dev.chieppa.stretch.theme.Stretch
import kotlinx.coroutines.flow.filterNotNull

/**
 * A pane's change in Stretch's diff viewer, side by side or in one column and wrapping long lines or not as the pane
 * was left, scrolled back to where it was. Find matches draw in place, the current one set apart; threads sit under
 * their lines with a dot in the gutter and a mark on the ruler. Clicking a line number, or dragging over several,
 * starts a comment there.
 */
@Composable
fun DiffBody(pane: Pane, diff: FileDiff, workbench: Workbench, find: FindState, modifier: Modifier = Modifier) {
    val id = pane.id.value
    val ui = workbench.state.paneState(id)
    val wrap = workbench.wrap(id)
    val viewer = remember(id, diff) { DiffViewerState(split = ui.split, wrap = wrap) }
    LaunchedEffect(viewer, ui.split) { viewer.split = ui.split }
    LaunchedEffect(viewer, wrap) { viewer.wrap = wrap }
    val lines = remember(diff) { DiffLines.of(diff) }
    LaunchedEffect(find.query, lines) { find.search(lines.map { it.text }) }
    val matches = remember(find.matches, find.open, lines) {
        if (!find.open) emptyList() else ViewerMatches.inDiff(find.matches, lines)
    }
    val current = matches.getOrNull(find.current)
    val threads = paneThreads(workbench, pane, DiffLines.added(diff), lines.size)
    val threadsAt = remember(threads) { threads.groupBy { it.side to it.line } }
    val threadInk = Stretch.colors.warnInk
    val marks = remember(threads, threadInk) { threads.map { DiffMark(0, ViewerMatches.sideOf(it.side), it.line, threadInk) } }

    fun key(side: DiffSide, number: Int?) = number?.let { (if (side == DiffSide.Old) LineSide.Old else LineSide.New) to it }
    val slots = DiffSlots(
        gutterMark = { ref -> threadsAt[key(ref.side, ref.number)]?.firstOrNull()?.let { GutterMark(it.state) } },
        lineAnnotation = { ref -> threadsAt[key(ref.side, ref.number)].orEmpty().forEach { PaneThread(it, workbench) } },
        onLineRangeSelect = { range ->
            PaneComments.start(workbench, pane, if (range.side == DiffSide.Old) LineSide.Old else LineSide.New, range.start, range.end)
            viewer.lineSelection = null
        },
    )
    DiffViewer(
        listOf(diff),
        modifier,
        viewer,
        showBar = false,
        slots = slots,
        matches = matches,
        currentMatch = current,
        marks = marks,
        changeWash = workbench.preferences.changeWash,
    )

    val commands = remember(viewer, find, lines) {
        val shared = find
        object : PaneCommands {
            override val find = shared
            override suspend fun nextChange() = viewer.nextChange()
            override suspend fun previousChange() = viewer.previousChange()
            override fun openFind() = find.show()
            override suspend fun moveMatch(step: Int) {
                val match = find.move(step) ?: return
                ViewerMatches.inDiff(listOf(match), lines).firstOrNull()?.let { viewer.scrollToMatch(it) }
            }
        }
    }
    DisposableEffect(id, commands) {
        workbench.paneCommands[id] = commands
        onDispose { if (workbench.paneCommands[id] === commands) workbench.paneCommands.remove(id) }
    }
    LaunchedEffect(viewer) {
        withFrameNanos {}
        workbench.state.paneLines[id]?.takeIf { it > 1 }?.let { viewer.scrollToLine(0, DiffSide.New, it) }
        snapshotFlow { viewer.firstVisibleLine(DiffSide.New)?.second }.filterNotNull().collect { workbench.state.paneLines[id] = it }
    }
}
