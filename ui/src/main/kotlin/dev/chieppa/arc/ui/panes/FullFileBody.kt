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
import dev.chieppa.arc.ui.conversation.LineChooser
import dev.chieppa.arc.ui.conversation.PaneComments
import dev.chieppa.arc.ui.conversation.ThreadReveal
import dev.chieppa.arc.ui.conversation.PaneThread
import dev.chieppa.arc.ui.conversation.paneThreads
import dev.chieppa.arc.ui.find.FindState
import dev.chieppa.arc.ui.find.ViewerMatches
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.code.CodeLanguages
import dev.chieppa.stretch.code.CodeMark
import dev.chieppa.stretch.code.CodeText
import dev.chieppa.stretch.code.CodeViewer
import dev.chieppa.stretch.code.CodeViewerSlots
import dev.chieppa.stretch.code.CodeViewerState
import dev.chieppa.stretch.code.LineTone
import dev.chieppa.stretch.diff.FileDiff
import dev.chieppa.stretch.theme.Stretch

/**
 * A whole file in Stretch's code viewer, filling the pane, its changed lines washed and barred, wrapping long lines or
 * not as the pane was left and scrolled back to where it was. Find matches draw in place, the current one set apart.
 * [diff] gives the changed lines to step between; [removed] shows a deleted file as it was, every line removed.
 * Clicking a line number starts a comment there, or asks whether to start one or open a thread on a line that has
 * threads. A thread jumped to from a list scrolls into view.
 */
@Composable
fun FullFileBody(pane: Pane, text: String, diff: FileDiff?, workbench: Workbench, find: FindState, modifier: Modifier = Modifier, removed: Boolean = false) {
    val id = pane.id.value
    val code = remember(text) { CodeText.of(text) }
    val wrap = workbench.wrap(id)
    val viewer = remember(id, text) { CodeViewerState(wrap) }
    LaunchedEffect(viewer, wrap) { viewer.wrap = wrap }
    val changed = remember(diff, removed) { if (removed) (1..code.lineCount).toSet() else diff?.let(DiffLines::added).orEmpty().toSet() }
    val changeStarts = remember(changed) { changed.sorted().filter { it - 1 !in changed } }
    LaunchedEffect(find.query, code) { find.search(code.lines) }
    val matches = remember(find.matches, find.open) { if (!find.open) emptyList() else ViewerMatches.inCode(find.matches) }
    val current = matches.getOrNull(find.current)
    val threads = paneThreads(workbench, pane, changed.toList(), code.lineCount)
    val threadsAt = remember(threads) { threads.filter { it.side == LineSide.New }.groupBy { it.line } }
    val threadInk = Stretch.colors.warnInk
    val marks = remember(threadsAt, threadInk) { threadsAt.keys.map { CodeMark(it, threadInk) } }
    val choice = workbench.threadViews.choosing?.takeIf { it.paneId == id }
    CodeViewer(
        code,
        modifier,
        viewer,
        language = CodeLanguages.forPath(pane.path),
        tones = { line -> if (line in changed) (if (removed) LineTone.Removed else LineTone.Added) else null },
        slots = CodeViewerSlots(
            gutterMark = { line -> threadsAt[line]?.firstOrNull()?.let { GutterMark(it.state) } },
            lineAnnotation = { line ->
                if (choice?.side == LineSide.New && choice.line == line) {
                    LineChooser(workbench, threadsAt[line].orEmpty()) { PaneComments.start(workbench, pane, LineSide.New, line) }
                }
                threadsAt[line].orEmpty().forEach { PaneThread(it, workbench) }
            },
            onLineNumberClick = { line -> if (!removed) PaneComments.click(workbench, pane, LineSide.New, line, line, threadsAt[line].orEmpty()) },
        ),
        matches = matches,
        currentMatch = current,
        marks = marks,
        changeWash = workbench.preferences.changeWash,
    )
    val commands = remember(viewer, find, changeStarts) {
        val shared = find
        object : PaneCommands {
            override val find = shared
            override suspend fun nextChange() {
                changeStarts.firstOrNull { it > viewer.firstVisibleLine + LEAD_IN }?.let { viewer.scrollToLine(it) }
            }

            override suspend fun previousChange() {
                changeStarts.lastOrNull { it < viewer.firstVisibleLine + LEAD_IN }?.let { viewer.scrollToLine(it) }
            }

            override fun openFind() = find.show()

            override suspend fun moveMatch(step: Int) {
                val match = find.move(step) ?: return
                ViewerMatches.inCode(listOf(match)).firstOrNull()?.let { viewer.scrollToMatch(it) }
            }
        }
    }
    DisposableEffect(id, commands) {
        workbench.paneCommands[id] = commands
        onDispose { if (workbench.paneCommands[id] === commands) workbench.paneCommands.remove(id) }
    }
    LaunchedEffect(viewer) {
        withFrameNanos {}
        workbench.state.paneLines[id]?.takeIf { it > 1 }?.let { viewer.alignLine(it) }
        snapshotFlow { viewer.firstVisibleLine }.collect { workbench.state.paneLines[id] = it }
    }
    val reveal = workbench.threadViews.reveal?.takeIf { it.source == pane.source && it.path == pane.path && it.side == LineSide.New }
    LaunchedEffect(viewer, reveal) {
        reveal ?: return@LaunchedEffect
        withFrameNanos {}
        viewer.scrollToLine((reveal.line - ThreadReveal.LEAD_IN).coerceAtLeast(1))
        workbench.threadViews.reveal = null
    }
}

/** Lines a change stepped to keeps showing above it, matching the viewer's own lead-in. */
private const val LEAD_IN = 3
