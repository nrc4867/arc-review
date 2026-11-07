package dev.chieppa.arc.ui.revisions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import dev.chieppa.arc.core.conversation.ThreadState
import dev.chieppa.arc.core.revision.CellKind
import dev.chieppa.arc.core.revision.MatrixCell
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.matrix_legend_after_review
import dev.chieppa.arc.design.generated.matrix_legend_changed
import dev.chieppa.arc.design.generated.matrix_legend_reviewed
import dev.chieppa.arc.design.generated.thread_addressed
import dev.chieppa.arc.design.generated.thread_open
import dev.chieppa.arc.design.generated.thread_resolved
import dev.chieppa.arc.ui.util.ArcSizes
import dev.chieppa.arc.ui.util.topRule
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchSpacing
import org.jetbrains.compose.resources.stringResource

/** What the revision matrix's cells and dots mean: changed, reviewed there, changed after review, and the thread states. */
@Composable
fun MatrixLegend() {
    val c = Stretch.colors
    val style = Stretch.type.bodySm.merge(TextStyle(color = c.inkControl))
    @Composable
    fun Entry(label: String, swatch: @Composable () -> Unit) {
        Row(horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space6), verticalAlignment = Alignment.CenterVertically) {
            swatch()
            BasicText(label, style = style)
        }
    }
    FlowRow(
        Modifier.fillMaxWidth().topRule(c.linePanel).padding(horizontal = StretchSpacing.space14, vertical = StretchSpacing.space10),
        horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space14),
        verticalArrangement = Arrangement.spacedBy(StretchSpacing.space4),
    ) {
        val swatch = Modifier.size(ArcSizes.legendSwatchWidth, ArcSizes.legendSwatchHeight)
        Entry(stringResource(Res.string.matrix_legend_changed)) { MatrixCellView(MatrixCell(CellKind.Changed), false, {}, swatch, interactive = false) }
        Entry(stringResource(Res.string.matrix_legend_reviewed)) { MatrixCellView(MatrixCell(CellKind.Reviewed), false, {}, swatch, interactive = false) }
        Entry(stringResource(Res.string.matrix_legend_after_review)) { MatrixCellView(MatrixCell(CellKind.ChangedAfterReview), false, {}, swatch, interactive = false) }
        Entry(stringResource(Res.string.thread_open)) { ThreadDot(ThreadState.Open) }
        Entry(stringResource(Res.string.thread_addressed)) { ThreadDot(ThreadState.Addressed) }
        Entry(stringResource(Res.string.thread_resolved)) { ThreadDot(ThreadState.Resolved) }
    }
}
