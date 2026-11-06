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
import dev.chieppa.arc.ui.conversation.ThreadCard
import dev.chieppa.arc.ui.conversation.paneThreads
import dev.chieppa.arc.ui.find.FindState
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.code.LineTone
import dev.chieppa.stretch.diff.DiffSide
import dev.chieppa.stretch.diff.DiffSlots
import dev.chieppa.stretch.diff.DiffViewer
import dev.chieppa.stretch.diff.DiffViewerState
import dev.chieppa.stretch.diff.FileDiff
import kotlinx.coroutines.flow.filterNotNull

/**
 * A pane's change in Stretch's diff viewer, side by side or in one column as the pane was left, scrolled back to
 * where it was. Lines with a find match are washed, the current one more strongly; threads sit under their lines with
 * a dot in the gutter. The file's header holds the mark-reviewed button.
 */
@Composable
fun DiffBody(pane: Pane, diff: FileDiff, workbench: Workbench, find: FindState, modifier: Modifier = Modifier) {
    val id = pane.id.value
    val ui = workbench.state.paneState(id)
    val viewer = remember(id, diff) { DiffViewerState(split = ui.split) }
    LaunchedEffect(viewer, ui.split) { viewer.split = ui.split }
    val lines = remember(diff) { DiffLines.of(diff) }
    LaunchedEffect(find.query, lines) { find.search(lines.map { it.text }) }
    val matched = remember(find.matches, lines) { find.matches.map { lines[it.index].side to lines[it.index].number }.toSet() }
    val current = find.currentMatch?.let { lines[it.index].side to lines[it.index].number }
    val threads = paneThreads(workbench, pane, DiffLines.added(diff), lines.size)
    val threadsAt = remember(threads) { threads.groupBy { it.side to it.line } }

    fun key(side: DiffSide, number: Int?) = number?.let { (if (side == DiffSide.Old) LineSide.Old else LineSide.New) to it }
    val slots = DiffSlots(
        lineTone = { ref ->
            when (key(ref.side, ref.number)) {
                null -> null
                current -> LineTone.Selected
                in matched -> LineTone.Emphasis
                else -> null
            }
        },
        gutterMark = { ref -> threadsAt[key(ref.side, ref.number)]?.firstOrNull()?.let { GutterMark(it.state, changed = false) } },
        lineAnnotation = { ref ->
            threadsAt[key(ref.side, ref.number)].orEmpty().forEach { thread ->
                ThreadCard(
                    thread,
                    workbench.threadViews.expanded(thread),
                    { workbench.threadViews.toggle(thread) },
                    onAdopt = { workbench.threadViews.adopted += thread.id },
                    onDismiss = { workbench.threadViews.dismissed += thread.id },
                )
            }
        },
        fileActions = { if (pane.source != null) MarkReviewedButton(pane, workbench) },
    )
    DiffViewer(listOf(diff), modifier, viewer, showBar = false, slots = slots)

    val commands = remember(viewer, find, lines) {
        val shared = find
        object : PaneCommands {
            override val find = shared
            override suspend fun nextChange() = viewer.nextChange()
            override suspend fun previousChange() = viewer.previousChange()
            override fun openFind() = find.show()
            override suspend fun moveMatch(step: Int) {
                val match = find.move(step) ?: return
                val line = lines[match.index]
                viewer.scrollToLine(0, if (line.side == LineSide.Old) DiffSide.Old else DiffSide.New, line.number)
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
