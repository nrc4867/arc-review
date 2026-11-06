package dev.chieppa.arc.ui.panes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import dev.chieppa.arc.core.pane.Pane
import dev.chieppa.arc.core.pane.PaneView
import dev.chieppa.arc.design.ArcIcons
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.action_find_in_file
import dev.chieppa.arc.design.generated.action_next_change
import dev.chieppa.arc.design.generated.action_previous_change
import dev.chieppa.arc.design.generated.pane_diff
import dev.chieppa.arc.design.generated.pane_full_file
import dev.chieppa.arc.design.generated.pane_pin
import dev.chieppa.arc.design.generated.pane_rendered
import dev.chieppa.arc.design.generated.pane_repo_separator
import dev.chieppa.arc.design.generated.pane_source
import dev.chieppa.arc.design.generated.pane_split
import dev.chieppa.arc.design.generated.pane_unified
import dev.chieppa.arc.design.generated.pane_unpin
import dev.chieppa.arc.design.generated.pane_wrap
import dev.chieppa.arc.ui.find.FindState
import dev.chieppa.arc.ui.util.ArcSizes
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchIcons
import dev.chieppa.stretch.theme.StretchSizes
import dev.chieppa.stretch.theme.StretchSpacing
import dev.chieppa.stretch.ui.IconButton
import dev.chieppa.stretch.ui.PathLabel
import dev.chieppa.stretch.ui.Segmented
import org.jetbrains.compose.resources.stringResource

/**
 * The row under a pane's tabs, its file's header: the repo and path, shortened from the middle, a chip for an added,
 * deleted, unchanged or binary file, its revisions, how it shows (diff, full file or rendered), side by side or one
 * column, moving between changes, wrapping, find, pinning, and marking it reviewed. [kind] is null until the text is
 * read. A narrow pane leaves out the revisions and the mark key's cap, so the path keeps its room.
 */
@Composable
fun PaneToolRow(pane: Pane, kind: FileKind?, binary: Boolean, workbench: Workbench, find: FindState, modifier: Modifier = Modifier) {
    val c = Stretch.colors
    val id = pane.id.value
    BoxWithConstraints(modifier.fillMaxWidth()) {
        val wide = maxWidth >= ArcSizes.toolRowWide
        val roomy = maxWidth >= ArcSizes.toolRowRoomy
        Row(
            Modifier.fillMaxWidth().height(ArcSizes.toolRow).background(c.surfacePanel).padding(horizontal = StretchSpacing.space8),
            horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space6),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val style = Stretch.type.valueSm
            Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space4), verticalAlignment = Alignment.CenterVertically) {
                if (pane.repo != workbench.state.active?.repo) {
                    BasicText(stringResource(Res.string.pane_repo_separator, pane.repo.value), style = style.merge(TextStyle(color = c.inkMuted)), maxLines = 1)
                }
                PathLabel(pane.path, style, c.inkStrong, Modifier.weight(1f, fill = false))
            }
            kind?.let { FileKindChip(it, binary) }
            if (roomy) RevisionChip(pane)
            val views = kind?.let { PaneViews.of(pane, it, binary) }.orEmpty()
            val view = kind?.let { PaneViews.shown(pane, it, binary) }
            if (views.size > 1 && view != null) {
                val labels = mapOf(
                    PaneView.Diff to stringResource(Res.string.pane_diff),
                    PaneView.FullFile to stringResource(if (pane.hasChange) Res.string.pane_full_file else Res.string.pane_source),
                    PaneView.Rendered to stringResource(Res.string.pane_rendered),
                )
                Segmented(views, view, { workbench.panes.setView(id, it) }, { labels.getValue(it) })
            }
            if (view == PaneView.Diff) {
                val split = workbench.state.paneState(id).split
                IconButton(StretchIcons.SplitView, stringResource(Res.string.pane_split), { workbench.panes.setSplit(id, true) }, size = StretchSizes.seg, on = split)
                IconButton(StretchIcons.UnifiedView, stringResource(Res.string.pane_unified), { workbench.panes.setSplit(id, false) }, size = StretchSizes.seg, on = !split)
            }
            if (view != null && view != PaneView.Rendered && !binary) {
                if (kind.hasDiff) {
                    IconButton(StretchIcons.ChevronLeft, stringResource(Res.string.action_previous_change), { workbench.runOnPane(id) { previousChange() } }, size = StretchSizes.seg)
                    IconButton(StretchIcons.ChevronRight, stringResource(Res.string.action_next_change), { workbench.runOnPane(id) { nextChange() } }, size = StretchSizes.seg)
                }
                val wrap = workbench.wrap(id)
                IconButton(ArcIcons.Wrap, stringResource(Res.string.pane_wrap), { workbench.panes.setWrap(id, !wrap) }, size = StretchSizes.seg, on = wrap)
                IconButton(StretchIcons.Search, stringResource(Res.string.action_find_in_file), { if (find.open) find.open = false else find.show() }, size = StretchSizes.seg, on = find.open)
            }
            IconButton(StretchIcons.Pin, stringResource(if (pane.pinned) Res.string.pane_unpin else Res.string.pane_pin), { workbench.panes.togglePin(id) }, size = StretchSizes.seg, on = pane.pinned)
            if (pane.source != null && pane.hasChange) MarkReviewedButton(pane, workbench, showKeys = wide)
        }
    }
}
