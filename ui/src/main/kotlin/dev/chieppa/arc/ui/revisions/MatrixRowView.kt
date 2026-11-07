package dev.chieppa.arc.ui.revisions

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import dev.chieppa.arc.core.revision.MatrixRow
import dev.chieppa.arc.ui.util.ArcSizes
import dev.chieppa.arc.ui.util.gridRules
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchSpacing

/**
 * One file's row of the revision matrix: its folder, quiet, and its name, then a cell per revision, [columnWidth]
 * wide. [selected] is the revision whose cell has the ring, if one in this row does; [onSelect] gets a cell's index.
 */
@Composable
fun MatrixRowView(row: MatrixRow, columnWidth: Dp, selected: Int?, onSelect: (Int) -> Unit) {
    val c = Stretch.colors
    val folder = row.path.substringBeforeLast('/', "")
    val name = row.path.substringAfterLast('/')
    Row(Modifier.height(ArcSizes.matrixRow)) {
        Row(
            Modifier.width(ArcSizes.matrixFileColumn).height(ArcSizes.matrixRow).gridRules(c.chartGrid).padding(horizontal = StretchSpacing.space12),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (folder.isNotEmpty()) {
                BasicText(
                    "$folder/",
                    Modifier.weight(1f, fill = false),
                    style = Stretch.type.bodySm.merge(TextStyle(color = c.inkSubtle)),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            BasicText(name, style = Stretch.type.bodySm.merge(TextStyle(color = c.inkStrong)), maxLines = 1)
        }
        row.cells.forEachIndexed { i, cell ->
            Box(
                Modifier.width(columnWidth).height(ArcSizes.matrixRow).gridRules(c.chartGrid).padding(horizontal = StretchSpacing.space6),
                contentAlignment = Alignment.Center,
            ) {
                MatrixCellView(cell, selected == i, { onSelect(i) })
            }
        }
    }
}
