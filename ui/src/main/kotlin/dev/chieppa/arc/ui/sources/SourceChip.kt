package dev.chieppa.arc.ui.sources

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
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
import dev.chieppa.arc.design.ArcIcons
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.sources_changed_since
import dev.chieppa.arc.design.generated.sources_progress
import dev.chieppa.arc.ui.util.ArcSizes
import dev.chieppa.stretch.theme.Icon
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchSpacing
import dev.chieppa.stretch.ui.Tooltip
import org.jetbrains.compose.resources.stringResource

/**
 * One source in the strip: its kind, ref and title, how much of it is reviewed and how many files changed since they
 * were reviewed. The [active] source is outlined; a source that could not be read is edged in the error color.
 */
@Composable
fun SourceChip(
    source: Source,
    title: String,
    progress: ReviewProgress?,
    changedSince: Int,
    active: Boolean,
    failed: Boolean,
    onClick: () -> Unit,
) {
    val c = Stretch.colors
    val shape = Stretch.shapes.ctlShape
    val edge = when {
        failed -> c.errorEdge
        active -> c.accent
        else -> c.linePanel.copy(alpha = 0f)
    }
    Row(
        Modifier
            .height(ArcSizes.sourceChipHeight)
            .clip(shape)
            .background(if (active) c.onFill else c.surfaceRail)
            .border(Stretch.stroke, edge, shape)
            .clickable(role = Role.Tab, onClick = onClick)
            .semantics { contentDescription = source.ref; selected = active }
            .padding(horizontal = StretchSpacing.space8),
        horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space6),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val ink = if (failed) c.errorInk else if (active) c.inkStrong else c.ink
        Icon(sourceIcon(source), ink)
        BasicText(source.ref, style = Stretch.type.valueSm.merge(TextStyle(color = ink, fontWeight = FontWeight.SemiBold)), maxLines = 1)
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
    }
}
