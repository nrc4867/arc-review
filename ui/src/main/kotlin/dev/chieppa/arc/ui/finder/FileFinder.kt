package dev.chieppa.arc.ui.finder

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.window.PopupProperties
import dev.chieppa.arc.core.pane.Revision
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.finder_placeholder
import dev.chieppa.arc.ui.util.ArcSizes
import dev.chieppa.arc.ui.util.Revisions
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.frame.MenuItem
import dev.chieppa.stretch.frame.searchLabels
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchIcons
import dev.chieppa.stretch.theme.StretchSpacing
import dev.chieppa.stretch.ui.Divider
import dev.chieppa.stretch.ui.StretchPopup
import dev.chieppa.stretch.ui.TextField
import dev.chieppa.stretch.ui.framedSurface
import org.jetbrains.compose.resources.stringResource

/**
 * Finds any file in the active source's tree at its head, changed or not, and opens it: a changed file as its change,
 * any other whole. Up and Down move, Enter opens, Esc closes.
 */
@Composable
fun FileFinder(workbench: Workbench) {
    if (!workbench.finderOpen) return
    val source = workbench.state.active ?: return
    val snapshot = workbench.state.snapshot(source) ?: return
    val head = Revision(snapshot.revisions.head.value)
    val files = produceState(emptyList<String>(), source, head) { value = workbench.data.listFiles(source.repo, head) }.value
    var query by remember { mutableStateOf("") }
    var index by remember { mutableIntStateOf(0) }
    val matches = remember(query, files) { searchLabels(query, files) { it } }
    val focus = remember { FocusRequester() }
    fun close() {
        workbench.finderOpen = false
    }
    fun open(path: String) {
        close()
        if (!workbench.panes.openChange(source, path, newTab = true)) workbench.panes.openFile(source.repo, head, path, source)
    }
    StretchPopup(TopCenter(ArcSizes.finderTop), ::close, PopupProperties(focusable = true), Stretch.shadows.float) {
        LaunchedEffect(Unit) { focus.requestFocus() }
        Column(
            Modifier
                .width(ArcSizes.finderWidth)
                .framedSurface(Stretch.shapes.cardShape, Stretch.colors.surfaceFloat, Stretch.colors.lineStrong, Stretch.stroke)
                .onPreviewKeyEvent { event ->
                    if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                    when (event.key) {
                        Key.DirectionDown -> index = (index + 1).coerceAtMost((minOf(matches.size, ArcSizes.finderRows) - 1).coerceAtLeast(0))
                        Key.DirectionUp -> index = (index - 1).coerceAtLeast(0)
                        Key.Enter, Key.NumPadEnter -> matches.getOrNull(index)?.let(::open)
                        Key.Escape -> close()
                        else -> return@onPreviewKeyEvent false
                    }
                    true
                },
        ) {
            Row(Modifier.fillMaxWidth().padding(StretchSpacing.space8), verticalAlignment = Alignment.CenterVertically) {
                TextField(
                    query,
                    { query = it; index = 0 },
                    Modifier.fillMaxWidth().focusRequester(focus),
                    placeholder = stringResource(Res.string.finder_placeholder, Revisions.short(head)),
                    icon = StretchIcons.Search,
                )
            }
            Divider()
            Column(Modifier.padding(StretchSpacing.space6)) {
                matches.take(ArcSizes.finderRows).forEachIndexed { i, path ->
                    val changed = snapshot.file(path) != null
                    MenuItem(path, { open(path) }, icon = if (changed) StretchIcons.Pen else StretchIcons.File, highlighted = i == index)
                }
            }
        }
    }
}
