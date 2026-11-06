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
    CodeViewer(
        code,
        modifier,
        viewer,
        language = CodeLanguages.forPath(pane.path),
        tones = { line -> if (line in changed) (if (removed) LineTone.Removed else LineTone.Added) else null },
        slots = CodeViewerSlots(
            gutterMark = { line -> threadsAt[line]?.firstOrNull()?.let { GutterMark(it.state) } },
            lineAnnotation = { line ->
                threadsAt[line].orEmpty().forEach { thread ->
                    ThreadCard(thread, workbench.threadViews.expanded(thread), { workbench.threadViews.toggle(thread) })
                }
            },
        ),
        matches = matches,
        currentMatch = current,
        marks = marks,
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
}

/** Lines a change stepped to keeps showing above it, matching the viewer's own lead-in. */
private const val LEAD_IN = 3
