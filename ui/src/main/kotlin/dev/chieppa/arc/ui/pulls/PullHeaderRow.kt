package dev.chieppa.arc.ui.pulls

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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.col_labels
import dev.chieppa.arc.design.generated.col_pull_request
import dev.chieppa.arc.design.generated.col_updated
import dev.chieppa.arc.design.generated.col_waiting
import dev.chieppa.arc.ui.util.ArcSizes
import dev.chieppa.arc.ui.util.bottomRule
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchSpacing
import org.jetbrains.compose.resources.stringResource

/** The column heads over a saved filter's list, lined up with [PullRow]'s [columns]. */
@Composable
fun PullHeaderRow(columns: PullColumns) {
    val c = Stretch.colors
    Row(
        Modifier.fillMaxWidth().heightIn(min = ArcSizes.pullHeader).background(c.surfacePanel).bottomRule(c.chartGrid).padding(horizontal = StretchSpacing.space12),
        horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space12),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.width(ArcSizes.newDot))
        Box(Modifier.width(ArcSizes.pullIcon))
        Eyebrow(stringResource(Res.string.col_pull_request), Modifier.weight(1f))
        if (columns.labels) Eyebrow(stringResource(Res.string.col_labels), Modifier.width(ArcSizes.labelsColumn))
        if (columns.waiting) Eyebrow(stringResource(Res.string.col_waiting), Modifier.width(ArcSizes.waitingColumn))
        Spacer(ArcSizes.checkColumn)
        if (columns.comments) Spacer(ArcSizes.commentsColumn)
        if (columns.updated) Eyebrow(stringResource(Res.string.col_updated), Modifier.width(ArcSizes.updatedColumn), TextAlign.End)
        Spacer(ArcSizes.actionColumn)
    }
}

@Composable
private fun Eyebrow(text: String, modifier: Modifier, align: TextAlign = TextAlign.Start) {
    BasicText(text.uppercase(), modifier, style = Stretch.type.eyebrow.merge(TextStyle(color = Stretch.colors.inkMuted, textAlign = align)), maxLines = 1)
}

@Composable
private fun Spacer(width: Dp) {
    Box(Modifier.width(width))
}
