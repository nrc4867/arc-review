package dev.chieppa.arc.ui.pulls

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import dev.chieppa.arc.design.ArcIcons
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.filter_new_since
import dev.chieppa.arc.design.generated.pull_add
import dev.chieppa.arc.design.generated.pull_comments
import dev.chieppa.arc.host.github.pulls.PullRequestSummary
import dev.chieppa.arc.host.github.pulls.WaitingOn
import dev.chieppa.arc.ui.util.ArcSizes
import dev.chieppa.arc.ui.util.bottomRule
import dev.chieppa.stretch.theme.Icon
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchIcons
import dev.chieppa.stretch.theme.StretchSpacing
import dev.chieppa.stretch.ui.Button
import dev.chieppa.stretch.ui.ButtonKind
import dev.chieppa.stretch.ui.Chip
import dev.chieppa.stretch.ui.ChipKind
import dev.chieppa.stretch.ui.Tooltip
import org.jetbrains.compose.resources.stringResource

/**
 * One pull request in a saved filter's list: a dot when it changed since the filter was last looked at, its title, ref
 * and author, labels, who it waits on (nothing while no one is asked to review), checks, comments and age, and at the
 * end the review that holds it ([holding]) or, when it can be added to the open review, [onAdd]'s button.
 */
@Composable
fun PullRow(
    pull: PullRequestSummary,
    ref: String,
    isNew: Boolean,
    waiting: WaitingOn,
    now: Long,
    holding: String?,
    onAdd: (() -> Unit)?,
) {
    val c = Stretch.colors
    Row(
        Modifier.fillMaxWidth().heightIn(min = ArcSizes.pullRow).bottomRule(c.chartGrid).padding(horizontal = StretchSpacing.space12),
        horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space12),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (isNew) {
            Tooltip(stringResource(Res.string.filter_new_since)) { Box(Modifier.size(ArcSizes.newDot).background(c.accent, CircleShape)) }
        } else {
            Box(Modifier.width(ArcSizes.newDot))
        }
        Icon(ArcIcons.PullRequest, c.inkControl)
        Column(Modifier.weight(1f)) {
            BasicText(pull.title, style = Stretch.type.body.merge(TextStyle(color = c.inkStrong, fontWeight = FontWeight.Medium)), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Row(horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space6), verticalAlignment = Alignment.CenterVertically) {
                BasicText(ref, style = Stretch.type.valueSm.merge(TextStyle(color = c.inkMuted)), maxLines = 1)
                pull.author?.let { author ->
                    Box(Modifier.size(ArcSizes.separatorDot).background(c.inkSubtle, CircleShape))
                    SmallAvatar(author)
                    BasicText(author, style = Stretch.type.chip.merge(TextStyle(color = c.inkMuted, fontWeight = FontWeight.Normal)), maxLines = 1)
                }
            }
        }
        Row(Modifier.width(ArcSizes.labelsColumn).clipToBounds(), horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space4)) {
            pull.labels.forEach { LabelPill(it) }
        }
        Box(Modifier.width(ArcSizes.waitingColumn)) { if (waiting != WaitingOn.Reviewers(0)) WaitingPill(waiting) }
        Box(Modifier.width(ArcSizes.checkColumn)) { CheckMark(pull.checks.state) }
        Tooltip(stringResource(Res.string.pull_comments)) {
            Row(Modifier.width(ArcSizes.commentsColumn), horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space4), verticalAlignment = Alignment.CenterVertically) {
                Icon(ArcIcons.Comment, c.inkMuted, size = ArcSizes.smallIcon)
                BasicText(pull.comments.toString(), style = Stretch.type.valueSm.merge(TextStyle(color = c.inkMuted)))
            }
        }
        BasicText(
            shortAge(pull.updatedAt.toEpochMilli(), now),
            Modifier.width(ArcSizes.updatedColumn),
            style = Stretch.type.chip.merge(TextStyle(color = c.inkSubtle, fontWeight = FontWeight.Normal, textAlign = TextAlign.End)),
            maxLines = 1,
        )
        Box(Modifier.width(ArcSizes.actionColumn), contentAlignment = Alignment.CenterEnd) {
            when {
                holding != null -> Chip(holding, kind = ChipKind.On, icon = StretchIcons.Layers)
                onAdd != null -> Button(stringResource(Res.string.pull_add), onAdd, Modifier.height(ArcSizes.addButton), kind = ButtonKind.Quiet, icon = StretchIcons.Plus)
            }
        }
    }
}
