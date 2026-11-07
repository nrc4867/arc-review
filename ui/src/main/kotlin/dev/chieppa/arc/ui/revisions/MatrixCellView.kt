package dev.chieppa.arc.ui.revisions

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import dev.chieppa.arc.core.revision.CellKind
import dev.chieppa.arc.core.revision.MatrixCell
import dev.chieppa.arc.design.ArcIcons
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.matrix_cell_after_review
import dev.chieppa.arc.design.generated.matrix_cell_changed
import dev.chieppa.arc.design.generated.matrix_cell_reviewed
import dev.chieppa.arc.design.generated.matrix_cell_unchanged
import dev.chieppa.arc.ui.util.ArcSizes
import dev.chieppa.stretch.theme.Icon
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchIcons
import dev.chieppa.stretch.theme.StretchSpacing
import org.jetbrains.compose.resources.stringResource

/**
 * One cell of the revision matrix: a button for a file changed in the revision, in the accent colors with a check
 * where it was reviewed and the warning colors where it changed after the review, or a small dot where it did not
 * change; with its thread counts, and a ring while [selected]. A click selects it, unless it only shows what a cell
 * looks like, not [interactive], as in the legend.
 */
@Composable
fun MatrixCellView(cell: MatrixCell, selected: Boolean, onSelect: () -> Unit, modifier: Modifier = Modifier, interactive: Boolean = true) {
    val c = Stretch.colors
    val shape = Stretch.shapes.segShape
    val (fill, edge, ink) = when (cell.kind) {
        CellKind.Changed -> Triple(c.surfaceControl, c.lineStrong, c.ink)
        CellKind.Reviewed -> Triple(c.onFill, c.onEdge, c.onInk)
        CellKind.ChangedAfterReview -> Triple(c.warnWash, c.warnEdge, c.warnInk)
        CellKind.Unchanged -> Triple(Color.Transparent, Color.Transparent, c.ink)
    }
    val label = stringResource(
        when (cell.kind) {
            CellKind.Changed -> Res.string.matrix_cell_changed
            CellKind.Reviewed -> Res.string.matrix_cell_reviewed
            CellKind.ChangedAfterReview -> Res.string.matrix_cell_after_review
            CellKind.Unchanged -> Res.string.matrix_cell_unchanged
        },
    )
    val ring = c.ringSelected
    val ringWidth = ArcSizes.matrixRing
    val face = modifier
        .fillMaxWidth()
        .height(ArcSizes.matrixCell)
        .drawWithContent {
            drawContent()
            if (selected) {
                val inset = ringWidth.toPx() / 2
                val corner = shape.topStart.toPx(size, this) + inset
                drawRoundRect(
                    ring,
                    Offset(-inset, -inset),
                    Size(size.width + 2 * inset, size.height + 2 * inset),
                    CornerRadius(corner),
                    style = Stroke(ringWidth.toPx()),
                )
            }
        }
        .clip(shape)
        .background(fill)
        .border(Stretch.stroke, edge, shape)
        .semantics { contentDescription = label }
    Row(
        if (interactive && cell.kind.changed) face.clickable(role = Role.Button, onClick = onSelect) else face,
        horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space6, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        when (cell.kind) {
            CellKind.Reviewed -> Icon(StretchIcons.Check, ink, size = ArcSizes.matrixCellIcon)
            CellKind.ChangedAfterReview -> Icon(ArcIcons.ChangedSince, ink, size = ArcSizes.matrixCellIcon)
            CellKind.Unchanged -> Box(Modifier.size(ArcSizes.unchangedDot).background(c.lineStrong, CircleShape))
            CellKind.Changed -> Unit
        }
        ThreadDots(cell.threads, ink)
    }
}
