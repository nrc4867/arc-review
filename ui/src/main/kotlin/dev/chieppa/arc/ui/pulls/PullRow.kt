package dev.chieppa.arc.ui.pulls

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
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
import dev.chieppa.arc.design.generated.pull_cloning
import dev.chieppa.arc.design.generated.pull_comments
import dev.chieppa.arc.design.generated.pull_open
import dev.chieppa.arc.host.github.pulls.PullRequestSummary
import dev.chieppa.arc.host.github.pulls.WaitingOn
import dev.chieppa.arc.ui.util.ArcSizes
import dev.chieppa.arc.ui.util.bottomRule
import dev.chieppa.stretch.frame.ContextMenuArea
import dev.chieppa.stretch.theme.Icon
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchSpacing
import dev.chieppa.stretch.ui.Chip
import dev.chieppa.stretch.ui.ChipKind
import dev.chieppa.stretch.ui.Spinner
import dev.chieppa.stretch.ui.Tooltip
import org.jetbrains.compose.resources.stringResource

/**
 * One pull request in a saved filter's list: a dot when it changed since the filter was last looked at, its title, ref
 * and author, labels, who it waits on (nothing while no one is asked to review), checks, comments and age, and at the
 * end a chip when it is [open] in the strip. Only the [columns] that fit show; the title takes the rest. While its repo
 * is being cloned, or its [clone] failed, that shows in place of the chip, a failure with its [cloneError] and opening
 * the Console through [onCloneError]. A click runs [onOpen]; a right-click opens
 * [menu].
 */
@Composable
fun PullRow(
    pull: PullRequestSummary,
    ref: String,
    isNew: Boolean,
    waiting: WaitingOn,
    now: Long,
    open: Boolean,
    columns: PullColumns,
    onOpen: () -> Unit,
    clone: CloneState? = null,
    cloneError: String? = null,
    onCloneError: () -> Unit = {},
    menu: @Composable ColumnScope.(close: () -> Unit) -> Unit,
) {
    val c = Stretch.colors
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    ContextMenuArea(menu) {
        Row(
            Modifier
                .fillMaxWidth()
                .heightIn(min = ArcSizes.pullRow)
                .background(if (hovered) c.surfaceControl else Color.Transparent)
                .bottomRule(c.chartGrid)
                .hoverable(interaction)
                .clickable(interaction, null, role = Role.Button, onClick = onOpen)
                .padding(horizontal = StretchSpacing.space12),
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
                    BasicText(ref, Modifier.weight(1f, fill = false), style = Stretch.type.valueSm.merge(TextStyle(color = c.inkMuted)), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    pull.author?.let { author ->
                        Box(Modifier.size(ArcSizes.separatorDot).background(c.inkSubtle, CircleShape))
                        SmallAvatar(author)
                        BasicText(author, Modifier.weight(1f, fill = false), style = Stretch.type.chip.merge(TextStyle(color = c.inkMuted, fontWeight = FontWeight.Normal)), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
            if (columns.labels) {
                Row(Modifier.width(ArcSizes.labelsColumn).clipToBounds(), horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space4)) {
                    pull.labels.forEach { LabelPill(it) }
                }
            }
            if (columns.waiting) Box(Modifier.width(ArcSizes.waitingColumn)) { if (waiting != WaitingOn.Reviewers(0)) WaitingPill(waiting) }
            Box(Modifier.width(ArcSizes.checkColumn)) { CheckMark(pull.checks.state) }
            if (columns.comments) Tooltip(stringResource(Res.string.pull_comments)) {
                Row(Modifier.width(ArcSizes.commentsColumn), horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space4), verticalAlignment = Alignment.CenterVertically) {
                    Icon(ArcIcons.Comment, c.inkMuted, size = ArcSizes.smallIcon)
                    BasicText(pull.comments.toString(), style = Stretch.type.valueSm.merge(TextStyle(color = c.inkMuted)))
                }
            }
            if (columns.updated) BasicText(
                shortAge(pull.updatedAt.toEpochMilli(), now),
                Modifier.width(ArcSizes.updatedColumn),
                style = Stretch.type.chip.merge(TextStyle(color = c.inkSubtle, fontWeight = FontWeight.Normal, textAlign = TextAlign.End)),
                maxLines = 1,
            )
            Box(Modifier.width(ArcSizes.actionColumn), contentAlignment = Alignment.CenterEnd) {
                when {
                    clone == CloneState.Cloning -> Spinner(stringResource(Res.string.pull_cloning, pull.repo.fullName))
                    clone == CloneState.Failed -> CloneFailedMark(pull.repo.fullName, cloneError, onCloneError)
                    open -> Chip(stringResource(Res.string.pull_open), kind = ChipKind.On)
                }
            }
        }
    }
}
