package dev.chieppa.arc.ui.sources

import dev.chieppa.arc.ui.util.SourceLabels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import dev.chieppa.arc.ui.util.extendsSelection
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import dev.chieppa.arc.core.mark.ReviewProgress
import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.host.github.pulls.CheckState
import dev.chieppa.arc.host.github.pulls.PullState
import dev.chieppa.arc.ui.util.ArcAlphas
import dev.chieppa.arc.ui.pulls.CheckMark
import dev.chieppa.arc.design.ArcIcons
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.sources_changed_since
import dev.chieppa.arc.design.generated.sources_progress
import dev.chieppa.arc.design.generated.sources_close
import dev.chieppa.stretch.frame.ContextMenuArea
import dev.chieppa.stretch.theme.StretchIcons
import dev.chieppa.stretch.ui.FieldIcon
import dev.chieppa.arc.ui.util.ArcSizes
import dev.chieppa.stretch.theme.Icon
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchSpacing
import dev.chieppa.stretch.ui.Tooltip
import org.jetbrains.compose.resources.stringResource

/**
 * One source in the strip: its kind, ref and title, how much of it is reviewed and how many files changed since they
 * were reviewed, and for a pull request its [checks]. A [selected] source, the active one or one added beside it, is
 * outlined; a source that could not be read is edged in the error color, and a pull request whose [pullState] is merged
 * or closed is dimmed, its ref's tooltip saying which. A click runs [onClick], and with Ctrl (Cmd on macOS) held
 * [onToggleSelected]. Its close button runs [onClose], and a right-click opens [menu].
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SourceChip(
    source: Source,
    title: String,
    progress: ReviewProgress?,
    changedSince: Int,
    selected: Boolean,
    failed: Boolean,
    checks: CheckState?,
    pullState: PullState?,
    onClick: () -> Unit,
    onToggleSelected: () -> Unit,
    onClose: () -> Unit,
    menu: @Composable ColumnScope.(close: () -> Unit) -> Unit,
) {
    val c = Stretch.colors
    val shape = Stretch.shapes.ctlShape
    val edge = when {
        failed -> c.errorEdge
        selected -> c.accent
        else -> c.linePanel.copy(alpha = 0f)
    }
    var extending by remember { mutableStateOf(false) }
    ContextMenuArea(menu) {
        Row(
            Modifier
                .alpha(if (pullState?.ended == true) ArcAlphas.ENDED else 1f)
                .height(ArcSizes.sourceChipHeight)
                .clip(shape)
                .background(if (selected) c.onFill else c.surfaceRail)
                .border(Stretch.stroke, edge, shape)
                .onPointerEvent(PointerEventType.Press, PointerEventPass.Initial) { extending = it.keyboardModifiers.extendsSelection }
                .clickable(role = Role.Tab) {
                    if (extending) onToggleSelected() else onClick()
                    extending = false
                }
                .semantics { contentDescription = source.ref; this.selected = selected }
                .padding(start = StretchSpacing.space8, end = StretchSpacing.space2),
            horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space6),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val ink = if (failed) c.errorInk else if (selected) c.inkStrong else c.ink
            Icon(sourceIcon(source), ink)
            val label = @Composable { BasicText(SourceLabels.of(source), style = Stretch.type.valueSm.merge(TextStyle(color = ink, fontWeight = FontWeight.SemiBold)), maxLines = 1) }
            pullStateText(pullState)?.let { Tooltip(it, content = label) } ?: label()
            if (title.isNotEmpty()) {
                BasicText(
                    title,
                    Modifier.widthIn(max = ArcSizes.sourceTitleMax),
                    style = Stretch.type.bodySm.merge(TextStyle(color = c.inkMuted)),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (progress != null) {
                ProgressRing(if (progress.total == 0) 1f else progress.reviewed.toFloat() / progress.total)
                BasicText(
                    stringResource(Res.string.sources_progress, progress.reviewed, progress.total),
                    style = Stretch.type.valueSm.merge(TextStyle(color = c.inkMuted)),
                )
            }
            if (changedSince > 0) {
                Tooltip(stringResource(Res.string.sources_changed_since)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space2), verticalAlignment = Alignment.CenterVertically) {
                        Icon(ArcIcons.ChangedSince, c.warnInk, size = ArcSizes.ring)
                        BasicText(changedSince.toString(), style = Stretch.type.valueSm.merge(TextStyle(color = c.warnInk)))
                    }
                }
            }
            checks?.let { CheckMark(it) }
            FieldIcon(StretchIcons.Close, contentDescription = stringResource(Res.string.sources_close), onClick = onClose)
        }
    }
}
