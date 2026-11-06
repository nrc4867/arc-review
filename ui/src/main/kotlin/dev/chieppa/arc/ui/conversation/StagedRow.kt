package dev.chieppa.arc.ui.conversation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import dev.chieppa.arc.core.conversation.StagedComment
import dev.chieppa.arc.core.conversation.StagedRoute
import dev.chieppa.arc.design.ArcIcons
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.staged_edit
import dev.chieppa.arc.design.generated.staged_in_summary
import dev.chieppa.arc.design.generated.staged_line_gone
import dev.chieppa.arc.design.generated.staged_menu
import dev.chieppa.arc.design.generated.staged_state
import dev.chieppa.arc.ui.util.ArcSizes
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchIcons
import dev.chieppa.stretch.theme.StretchSizes
import dev.chieppa.stretch.theme.StretchSpacing
import dev.chieppa.stretch.ui.IconButton
import org.jetbrains.compose.resources.stringResource

/**
 * One staged comment: how it will go up, its source, where it is and what it says. A comment whose line is gone is
 * washed in the warning color in place, its location struck through beside the revision that removed the line.
 */
@Composable
fun StagedRow(comment: StagedComment, modifier: Modifier = Modifier) {
    val c = Stretch.colors
    val gone = comment.lineGoneIn
    Row(
        modifier.fillMaxWidth().heightIn(min = StretchSizes.ctl).background(if (gone != null) c.warnWash else Color.Transparent).padding(horizontal = StretchSpacing.space12),
        horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space10),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.width(ArcSizes.stagedChip)) {
            when (comment.route) {
                StagedRoute.Line -> StateChip(stringResource(Res.string.staged_state), ArcIcons.Stage, stagedLook(false))
                StagedRoute.Summary -> StateChip(stringResource(Res.string.staged_in_summary), StretchIcons.Quote, StateLook(Color.Transparent, c.chipEdge, c.inkMuted))
            }
        }
        val mono = Stretch.type.valueSm
        BasicText(comment.source.ref, Modifier.width(ArcSizes.stagedSource), style = mono.merge(TextStyle(color = c.inkMuted)), maxLines = 1, overflow = TextOverflow.Ellipsis)
        Row(Modifier.width(ArcSizes.stagedLocation), horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space6), verticalAlignment = Alignment.CenterVertically) {
            val location = "${comment.path.substringAfterLast('/')}:${comment.line}"
            val style = if (gone != null) mono.merge(TextStyle(color = c.warnInk, textDecoration = TextDecoration.LineThrough)) else mono.merge(TextStyle(color = c.inkStrong))
            BasicText(location, style = style, maxLines = 1)
            if (gone != null) StateChip(stringResource(Res.string.staged_line_gone, gone), StretchIcons.Warn, stagedLook(true))
        }
        BasicText(comment.body, Modifier.weight(1f), style = Stretch.type.bodySm.merge(TextStyle(color = c.ink)), maxLines = 1, overflow = TextOverflow.Ellipsis)
        IconButton(StretchIcons.Pen, stringResource(Res.string.staged_edit), {}, size = StretchSizes.seg)
        IconButton(StretchIcons.More, stringResource(Res.string.staged_menu), {}, size = StretchSizes.seg)
    }
}
