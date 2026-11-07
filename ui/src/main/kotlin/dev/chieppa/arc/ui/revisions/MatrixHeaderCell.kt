package dev.chieppa.arc.ui.revisions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import dev.chieppa.arc.core.revision.SourceRevision
import dev.chieppa.arc.design.ArcIcons
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.matrix_force_pushed_tip
import dev.chieppa.arc.design.generated.matrix_head
import dev.chieppa.arc.design.generated.matrix_rebased
import dev.chieppa.arc.design.generated.matrix_rebased_onto
import dev.chieppa.arc.design.generated.matrix_rebased_tip
import dev.chieppa.arc.design.generated.revisions_number
import dev.chieppa.arc.host.github.pulls.CheckState
import dev.chieppa.arc.ui.pulls.CheckMark
import dev.chieppa.arc.ui.util.ArcSizes
import dev.chieppa.arc.ui.util.gridRules
import dev.chieppa.stretch.theme.Icon
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchSpacing
import dev.chieppa.stretch.ui.Chip
import dev.chieppa.stretch.ui.ChipKind
import dev.chieppa.stretch.ui.Tooltip
import org.jetbrains.compose.resources.stringResource

/**
 * A revision's column head: its number, its head's checks, a mark when it was force-pushed or a chip when it was
 * rebased (onto [baseBranch]), a chip on the [head] revision, then its short sha and when it became the head. A
 * [picked] revision, one the Commits drawer compares, is underlined.
 */
@Composable
fun MatrixHeaderCell(
    revision: SourceRevision,
    checks: CheckState?,
    head: Boolean,
    picked: Boolean,
    baseBranch: String?,
    nowSeconds: Long,
    modifier: Modifier = Modifier,
) {
    val c = Stretch.colors
    val ring = c.ringSelected
    val underline = ArcSizes.matrixRing
    Column(
        modifier
            .fillMaxHeight()
            .background(c.surfacePanel)
            .gridRules(c.chartGrid)
            .drawBehind {
                if (picked) {
                    val y = size.height - underline.toPx() / 2
                    drawLine(ring, Offset(0f, y), Offset(size.width, y), underline.toPx())
                }
            }
            .padding(StretchSpacing.space8),
        verticalArrangement = Arrangement.spacedBy(ArcSizes.threadCountGap, Alignment.Bottom),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space6), verticalAlignment = Alignment.CenterVertically) {
            BasicText(
                stringResource(Res.string.revisions_number, revision.number),
                style = Stretch.type.bodySm.merge(TextStyle(color = c.inkStrong, fontWeight = FontWeight.SemiBold)),
            )
            checks?.let { CheckMark(it) }
            if (revision.rewrote) {
                val tip = when {
                    !revision.rebased -> stringResource(Res.string.matrix_force_pushed_tip)
                    baseBranch != null -> stringResource(Res.string.matrix_rebased_onto, baseBranch)
                    else -> stringResource(Res.string.matrix_rebased_tip)
                }
                Tooltip(tip) {
                    if (revision.rebased) {
                        Chip(stringResource(Res.string.matrix_rebased), icon = ArcIcons.Force)
                    } else {
                        Icon(ArcIcons.Force, c.inkMuted, size = ArcSizes.matrixCellIcon, contentDescription = tip)
                    }
                }
            }
            if (head) Chip(stringResource(Res.string.matrix_head), kind = ChipKind.On)
        }
        BasicText(revision.head.short(), style = Stretch.type.valueSm.merge(TextStyle(color = c.inkMuted)), maxLines = 1)
        BasicText(revisionTime(revision.at, nowSeconds), style = Stretch.type.bodySm.merge(TextStyle(color = c.inkSubtle)), maxLines = 1)
    }
}
