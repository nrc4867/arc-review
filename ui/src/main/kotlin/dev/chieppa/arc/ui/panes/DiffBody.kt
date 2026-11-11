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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.produceState
import dev.chieppa.arc.ui.hints.PaneHintCard
import dev.chieppa.arc.ui.navigation.NameSpan
import dev.chieppa.arc.ui.navigation.NavigationPopover
import dev.chieppa.arc.ui.navigation.NavigationSizes
import dev.chieppa.arc.ui.navigation.NavigationTargets
import dev.chieppa.arc.ui.navigation.PaneCaret
import dev.chieppa.arc.ui.navigation.SyntaxTokens
import dev.chieppa.arc.ui.navigation.rememberDecorations
import dev.chieppa.stretch.code.CodeRange
import dev.chieppa.stretch.diff.DiffDecoration
import dev.chieppa.stretch.diff.DiffLineRef
import dev.chieppa.stretch.diff.DiffPosition
import dev.chieppa.stretch.diff.FileTokens
import kotlinx.coroutines.withContext
import dev.chieppa.stretch.diff.DiffMark
import dev.chieppa.stretch.diff.numberOn
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
 * starts a comment there; a click on one line that has threads asks whether to start one or open a thread. A thread
 * jumped to from a list scrolls into view. Each side is coloured by tree-sitter where it can read the file, its names
 * click through with Ctrl, and navigation's marks and providers' decorations draw on the side they belong to.
 */
@Composable
fun DiffBody(pane: Pane, text: PaneText, diff: FileDiff, workbench: Workbench, find: FindState, modifier: Modifier = Modifier) {
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

    fun key(side: DiffSide, number: Int?) = number?.let { lineSide(side) to it }
    // Split, each side's line is its own; in one column a line stands for both its numbers.
    fun sidesOf(ref: DiffLineRef) = if (viewer.split) listOf(ref.side) else DiffSide.entries
    fun here(ref: DiffLineRef) = sidesOf(ref).flatMap { side -> threadsAt[key(side, ref.line.numberOn(side))].orEmpty() }
    val choice = workbench.threadViews.choosing?.takeIf { it.paneId == id }
    val navigation = workbench.navigation
    val documents = remember(pane) { DiffSide.entries.associateWith { NavigationTargets.document(pane, lineSide(it)) } }
    val tokens = produceState(emptyMap<FileDiff, FileTokens>(), diff, text) {
        value = withContext(workbench.dispatchers.default) {
            val old = text.old?.let { SyntaxTokens.lines(documents.getValue(DiffSide.Old).path, it) }
            val new = text.new?.let { SyntaxTokens.lines(documents.getValue(DiffSide.New).path, it) }
            if (old == null && new == null) emptyMap() else mapOf(diff to FileTokens(old, new))
        }
    }.value
    val oldLines = remember(text.old) { text.old?.lines().orEmpty() }
    val newLines = remember(text.new) { text.new?.lines().orEmpty() }
    val oldDecorations = rememberDecorations(workbench, documents.getValue(DiffSide.Old).takeIf { pane.base != null }) { oldLines.getOrNull(it - 1)?.length }
    val newDecorations = rememberDecorations(workbench, documents.getValue(DiffSide.New)) { newLines.getOrNull(it - 1)?.length }
    val decorations = remember(oldDecorations, newDecorations) {
        oldDecorations.map { DiffDecoration(0, DiffSide.Old, it.range, it.style, it.tooltip) } +
            newDecorations.map { DiffDecoration(0, DiffSide.New, it.range, it.style, it.tooltip) }
    }
    val slots = DiffSlots(
        gutterMark = { ref -> here(ref).firstOrNull()?.let { GutterMark(it.state) } },
        lineAnnotation = { ref ->
            if (choice != null && sidesOf(ref).any { key(it, ref.line.numberOn(it)) == choice.side to choice.line }) {
                LineChooser(workbench, threadsAt[choice.side to choice.line].orEmpty()) { PaneComments.start(workbench, pane, choice.side, choice.line) }
            }
            here(ref).forEach { PaneThread(it, workbench) }
        },
        onLineRangeSelect = { range ->
            val side = lineSide(range.side)
            PaneComments.click(workbench, pane, side, range.start, range.end, threadsAt[side to range.start].orEmpty())
            viewer.lineSelection = null
        },
        symbolAt = { at -> navigation.nameAt(documents.getValue(at.side), at.line, at.column)?.let { CodeRange(it.line, it.start, it.end) } },
        onSymbolClick = { click ->
            val name = NameSpan(click.range.line, click.range.start, click.range.end)
            navigation.click(id, documents.getValue(click.position.side), name, click.bounds)
        },
        symbolCard = { at, _, card -> PaneHintCard(documents.getValue(at.side), at.line, at.column, card, workbench.hints) },
    )
    Box(modifier) {
        DiffViewer(
            listOf(diff),
            Modifier.fillMaxSize(),
            viewer,
            showBar = false,
            slots = slots,
            tokens = tokens,
            matches = matches,
            currentMatch = current,
            marks = marks,
            changeWash = workbench.preferences.changeWash,
            decorations = decorations,
        )
        NavigationPopover(workbench, id)
    }

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

            override suspend fun clickSymbolAtCaret() = viewer.clickSymbolAtCaret()
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
    LaunchedEffect(viewer) {
        snapshotFlow { viewer.caret }.collect { at -> at?.let { navigation.caret = PaneCaret(id, it.line, it.column) } }
    }
    val jump = navigation.reveal?.takeIf { jump -> DiffSide.entries.any { documents.getValue(it).let { d -> d.repo == jump.repo.value && d.revision == jump.revision && d.path == jump.path } } }
    LaunchedEffect(viewer, jump) {
        jump ?: return@LaunchedEffect
        withFrameNanos {}
        val side = if (documents.getValue(DiffSide.New).revision == jump.revision) DiffSide.New else DiffSide.Old
        viewer.scrollToLine(0, side, (jump.line - NavigationSizes.LEAD_IN).coerceAtLeast(1))
        viewer.caret = DiffPosition(0, side, jump.line, jump.column)
        navigation.reveal = null
    }
    val reveal = workbench.threadViews.reveal?.takeIf { it.source == pane.source && it.path == pane.path }
    LaunchedEffect(viewer, reveal) {
        reveal ?: return@LaunchedEffect
        withFrameNanos {}
        viewer.scrollToLine(0, if (reveal.side == LineSide.Old) DiffSide.Old else DiffSide.New, (reveal.line - ThreadReveal.LEAD_IN).coerceAtLeast(1))
        workbench.threadViews.reveal = null
    }
}

private fun lineSide(side: DiffSide) = if (side == DiffSide.Old) LineSide.Old else LineSide.New
