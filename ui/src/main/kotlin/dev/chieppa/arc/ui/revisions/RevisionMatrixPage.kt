package dev.chieppa.arc.ui.revisions

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import dev.chieppa.arc.core.pane.Page
import dev.chieppa.arc.ui.pulls.PullTimes
import dev.chieppa.arc.ui.pulls.currentTime
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.ui.Divider

/**
 * A source's revision matrix: its files against the heads it has had, which file changed in which revision, where
 * each was reviewed and where it changed after that, and the threads made on each. Arrow keys move the selected cell
 * and Enter opens its file at that revision. The revisions are read again whenever the source's head changes. Until
 * they are read the page is plain.
 */
@Composable
fun RevisionMatrixPage(page: Page.RevisionMatrix, workbench: Workbench, modifier: Modifier = Modifier) {
    val state = workbench.state
    val source = page.source
    val head = state.snapshot(source)?.revisions?.head
    val recorded = state.workspace.headsOf(source).size
    LaunchedEffect(source, head, recorded) { if (head != null) workbench.revisions.load(source) }
    val read = state.revisions[source] ?: return Box(modifier.fillMaxSize())
    val matrix = rememberMatrix(workbench, source, read)
    var onlyChanged by rememberSaveable(page.id.value) { mutableStateOf(false) }
    var withThreads by rememberSaveable(page.id.value) { mutableStateOf(false) }
    val rows = matrix.rows.filter { (!onlyChanged || it.changedAfterReview) && (!withThreads || it.hasThreads) }
    var picked by rememberSaveable(page.id.value) { mutableStateOf<Pair<String, Int>?>(null) }
    val selection = picked?.let { MatrixSelection(it.first, it.second) }?.takeIf { s -> rows.any { it.path == s.path } && s.index in matrix.revisions.indices }
        ?: MatrixSelection.first(rows)
    fun select(next: MatrixSelection) {
        picked = next.path to next.index
    }
    val focus = remember { FocusRequester() }
    val comparison = workbench.revisions.comparison(source)
    val pickedRevisions = listOfNotNull(comparison?.fromRevision, comparison?.toRevision).map { matrix.revisions.indexOf(it) }.toSet()
    fun openAt(at: MatrixSelection) = workbench.revisions.openAt(source, matrix.revisions[at.index], at.path)
    Column(
        modifier.fillMaxSize().focusRequester(focus).focusable().onKeyEvent { event ->
            if (event.type != KeyEventType.KeyDown || selection == null) return@onKeyEvent false
            when (event.key) {
                Key.DirectionUp -> select(selection.moved(rows, -1, 0))
                Key.DirectionDown -> select(selection.moved(rows, 1, 0))
                Key.DirectionLeft -> select(selection.moved(rows, 0, -1))
                Key.DirectionRight -> select(selection.moved(rows, 0, 1))
                Key.Enter, Key.NumPadEnter -> openAt(selection)
                else -> return@onKeyEvent false
            }
            true
        },
    ) {
        MatrixToolRow(matrix, onlyChanged, { onlyChanged = it }, withThreads, { withThreads = it })
        Divider()
        MatrixGrid(
            revisions = matrix.revisions,
            rows = rows,
            checks = read.checks,
            picked = pickedRevisions,
            baseBranch = baseBranchOf(workbench, source),
            nowSeconds = currentTime() / PullTimes.MILLIS_PER_SECOND,
            selection = selection,
            onSelect = {
                select(it)
                focus.requestFocus()
            },
            modifier = Modifier.weight(1f).fillMaxWidth(),
        )
        val row = selection?.let { s -> rows.firstOrNull { it.path == s.path } }
        if (selection != null && row != null) {
            SelectionLine(
                row,
                selection.index,
                onOpenDiff = { from -> workbench.revisions.openDiff(source, from?.let(matrix.revisions::get), matrix.revisions[selection.index], row.path) },
                onOpenAt = { openAt(selection) },
            )
        }
        MatrixLegend()
    }
}
