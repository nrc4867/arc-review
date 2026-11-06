package dev.chieppa.arc.ui.panes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import dev.chieppa.arc.core.pane.Pane
import dev.chieppa.arc.core.pane.PaneView
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.action_find_in_file
import dev.chieppa.arc.design.generated.action_next_change
import dev.chieppa.arc.design.generated.action_previous_change
import dev.chieppa.arc.design.generated.pane_diff
import dev.chieppa.arc.design.generated.pane_full_file
import dev.chieppa.arc.design.generated.pane_pin
import dev.chieppa.arc.design.generated.pane_rendered
import dev.chieppa.arc.design.generated.pane_source
import dev.chieppa.arc.design.generated.pane_split
import dev.chieppa.arc.design.generated.pane_unified
import dev.chieppa.arc.design.generated.pane_unpin
import dev.chieppa.arc.ui.find.FindState
import dev.chieppa.arc.ui.util.ArcSizes
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchIcons
import dev.chieppa.stretch.theme.StretchSizes
import dev.chieppa.stretch.theme.StretchSpacing
import dev.chieppa.stretch.ui.IconButton
import dev.chieppa.stretch.ui.Segmented
import org.jetbrains.compose.resources.stringResource

/**
 * The row under a pane's tabs: the file's repo and path, its revisions, how it shows (diff, full file or rendered),
 * side by side or one column, moving between changes, find, and pinning.
 */
@Composable
fun PaneToolRow(pane: Pane, workbench: Workbench, find: FindState, modifier: Modifier = Modifier) {
    val c = Stretch.colors
    val id = pane.id.value
    Row(
        modifier.fillMaxWidth().height(ArcSizes.toolRow).background(c.surfacePanel).padding(horizontal = StretchSpacing.space8),
        horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space6),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val folder = pane.path.substringBeforeLast('/', "")
        val inOtherRepo = pane.repo != workbench.state.active?.repo
        val path = buildAnnotatedString {
            withStyle(SpanStyle(color = c.inkMuted)) {
                if (inOtherRepo) append(pane.repo.value + " · ")
                if (folder.isNotEmpty()) append("$folder/")
            }
            withStyle(SpanStyle(color = c.inkStrong, fontWeight = FontWeight.SemiBold)) { append(pane.fileName) }
        }
        BasicText(path, Modifier.weight(1f), style = Stretch.type.valueSm.merge(TextStyle(color = c.ink)), maxLines = 1, overflow = TextOverflow.StartEllipsis)
        RevisionChip(pane)
        val views = PaneViews.of(pane)
        if (views.size > 1) {
            val labels = mapOf(
                PaneView.Diff to stringResource(Res.string.pane_diff),
                PaneView.FullFile to stringResource(if (pane.hasChange) Res.string.pane_full_file else Res.string.pane_source),
                PaneView.Rendered to stringResource(Res.string.pane_rendered),
            )
            Segmented(views, pane.view.takeIf { it in views } ?: views.first(), { workbench.panes.setView(id, it) }, { labels.getValue(it) })
        }
        if (pane.hasChange && pane.view == PaneView.Diff) {
            val split = workbench.state.paneState(id).split
            IconButton(StretchIcons.SplitView, stringResource(Res.string.pane_split), { workbench.panes.setSplit(id, true) }, size = StretchSizes.seg, on = split)
            IconButton(StretchIcons.UnifiedView, stringResource(Res.string.pane_unified), { workbench.panes.setSplit(id, false) }, size = StretchSizes.seg, on = !split)
        }
        if (pane.view != PaneView.Rendered) {
            if (pane.hasChange) {
                IconButton(StretchIcons.ChevronLeft, stringResource(Res.string.action_previous_change), { workbench.runOnPane(id) { previousChange() } }, size = StretchSizes.seg)
                IconButton(StretchIcons.ChevronRight, stringResource(Res.string.action_next_change), { workbench.runOnPane(id) { nextChange() } }, size = StretchSizes.seg)
            }
            IconButton(StretchIcons.Search, stringResource(Res.string.action_find_in_file), { if (find.open) find.open = false else find.show() }, size = StretchSizes.seg, on = find.open)
        }
        IconButton(StretchIcons.Pin, stringResource(if (pane.pinned) Res.string.pane_unpin else Res.string.pane_pin), { workbench.panes.togglePin(id) }, size = StretchSizes.seg, on = pane.pinned)
    }
}
