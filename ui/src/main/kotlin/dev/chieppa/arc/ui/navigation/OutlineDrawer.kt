package dev.chieppa.arc.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import dev.chieppa.arc.codeintel.model.OutlineAnswer
import dev.chieppa.arc.codeintel.model.OutlineRequest
import dev.chieppa.arc.core.conversation.LineSide
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.outline_filter
import dev.chieppa.arc.design.generated.outline_place
import dev.chieppa.arc.ui.util.Revisions
import dev.chieppa.arc.ui.util.SourceLabels
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchIcons
import dev.chieppa.stretch.theme.StretchSpacing
import dev.chieppa.stretch.ui.ScrollColumn
import dev.chieppa.stretch.ui.TextField
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.stringResource

/**
 * The Outline drawer: the focused pane's file and where it is from, a field filtering its symbols, and its symbols
 * nested as written, the one holding the caret set off. Picking one scrolls the pane to it. With no file it is plain.
 */
@Composable
fun OutlineDrawer(workbench: Workbench) {
    val c = Stretch.colors
    val pane = workbench.panes.focused ?: return
    val navigation = workbench.navigation
    val document = remember(pane) { NavigationTargets.document(pane, LineSide.New) }
    val status by navigation.service.status.collectAsState()
    val providers by navigation.service.intel.outlines.providers.collectAsState()
    val outline by produceState<OutlineAnswer?>(null, document, status, providers) {
        value = withContext(workbench.dispatchers.default) { navigation.service.router.outline(OutlineRequest(document)) }
    }
    var filter by remember(pane.id) { mutableStateOf("") }
    val rows = remember(outline, filter) { OutlineRows.of(outline?.symbols.orEmpty(), filter) }
    val caretLine = navigation.caret?.takeIf { it.paneId == pane.id.value }?.line
    val current = OutlineRows.current(rows, caretLine)
    Column(Modifier.fillMaxSize()) {
        Column(Modifier.padding(StretchSpacing.space12), verticalArrangement = Arrangement.spacedBy(StretchSpacing.space6)) {
            Row(horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space6), verticalAlignment = Alignment.CenterVertically) {
                BasicText(pane.fileName, style = Stretch.type.valueSm.merge(TextStyle(color = c.inkMuted)), maxLines = 1)
                val place = stringResource(Res.string.outline_place, pane.source?.let(SourceLabels::of) ?: pane.repo.value, Revisions.short(pane.revision))
                BasicText(place, style = Stretch.type.bodySm.merge(TextStyle(color = c.inkMuted)), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            TextField(filter, { filter = it }, Modifier.fillMaxWidth(), placeholder = stringResource(Res.string.outline_filter), icon = StretchIcons.Search)
        }
        ScrollColumn(Modifier.weight(1f).fillMaxWidth()) {
            for (row in rows) {
                OutlineRow(row, row == current) {
                    val at = row.symbol.range.start
                    navigation.reveal = NavReveal(pane.repo, pane.revision.value, pane.path, at.line, at.column)
                }
            }
        }
    }
}
