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
import dev.chieppa.stretch.code.CodeLanguages
import dev.chieppa.stretch.code.CodeText
import dev.chieppa.stretch.code.CodeViewer
import dev.chieppa.stretch.code.CodeViewerSlots
import dev.chieppa.stretch.code.CodeViewerState
import dev.chieppa.stretch.code.LineTone
import dev.chieppa.stretch.diff.FileDiff

/**
 * A whole file in Stretch's code viewer, its changed lines washed and barred in the gutter, scrolled back to where the
 * pane was. A plain query is found and marked by the viewer itself; whole-word and regular-expression matches wash
 * their lines. [changes] gives the changed lines to step between; [removed] shows the file as it was, for a deleted one.
 */
@Composable
fun FullFileBody(pane: Pane, text: String, diff: FileDiff?, workbench: Workbench, find: FindState, modifier: Modifier = Modifier, removed: Boolean = false) {
    val id = pane.id.value
    val code = remember(text) { CodeText.of(text) }
    val viewer = remember(id, text) { CodeViewerState() }
    val changed = remember(diff, removed) { if (removed) (1..code.lineCount).toSet() else diff?.let(DiffLines::added).orEmpty().toSet() }
    val changeStarts = remember(changed) { changed.sorted().filter { it - 1 !in changed } }
    LaunchedEffect(find.query, code) {
        find.search(code.lines)
        val plain = find.query.plain && find.open
        viewer.query = if (plain) find.query.text else ""
        viewer.matchCase = find.query.matchCase
    }
    LaunchedEffect(find.open) { if (!find.open) viewer.query = "" }
    val matched = remember(find.matches) { find.matches.map { it.index + 1 }.toSet() }
    val current = find.currentMatch?.let { it.index + 1 }
    val threads = paneThreads(workbench, pane, changed.toList(), code.lineCount)
    val threadsAt = remember(threads) { threads.filter { it.side == LineSide.New }.groupBy { it.line } }
    CodeViewer(
        code,
        modifier,
        viewer,
        language = CodeLanguages.forPath(pane.path),
        tones = { line ->
            when {
                find.open && !find.query.plain && line == current -> LineTone.Selected
                find.open && !find.query.plain && line in matched -> LineTone.Emphasis
                line in changed -> if (removed) LineTone.Removed else LineTone.Added
                else -> null
            }
        },
        slots = CodeViewerSlots(
            gutterMark = { line -> GutterMark(threadsAt[line]?.firstOrNull()?.state, changed = line in changed) },
            lineAnnotation = { line ->
                threadsAt[line].orEmpty().forEach { thread ->
                    ThreadCard(thread, workbench.threadViews.expanded(thread), { workbench.threadViews.toggle(thread) })
                }
            },
        ),
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
                if (find.query.plain) {
                    if (step > 0) viewer.nextMatch() else viewer.previousMatch()
                } else {
                    viewer.scrollToLine(match.index + 1)
                }
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
