package dev.chieppa.arc.ui.revisions

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.max
import dev.chieppa.arc.core.change.ObjectId
import dev.chieppa.arc.core.revision.MatrixRow
import dev.chieppa.arc.core.revision.SourceRevision
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.matrix_file
import dev.chieppa.arc.host.github.pulls.CheckState
import dev.chieppa.arc.ui.util.ArcSizes
import dev.chieppa.arc.ui.util.gridRules
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchSpacing
import org.jetbrains.compose.resources.stringResource

/**
 * The revision matrix's table: a head per revision over a row per file in [rows]. The revision columns share the width
 * the file column leaves, each at least [ArcSizes.matrixColumn]; when they do not fit, the table scrolls sideways.
 * Only the rows on screen are built.
 */
@Composable
fun MatrixGrid(
    revisions: List<SourceRevision>,
    rows: List<MatrixRow>,
    checks: Map<ObjectId, CheckState>,
    picked: Set<Int>,
    baseBranch: String?,
    nowSeconds: Long,
    selection: MatrixSelection?,
    onSelect: (MatrixSelection) -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = Stretch.colors
    BoxWithConstraints(modifier) {
        val count = revisions.size.coerceAtLeast(1)
        val column = max(ArcSizes.matrixColumn, (maxWidth - ArcSizes.matrixFileColumn) / count)
        val width = ArcSizes.matrixFileColumn + column * revisions.size
        Column(Modifier.horizontalScroll(rememberScrollState()).width(max(width, maxWidth)).fillMaxHeight()) {
            Row(Modifier.height(IntrinsicSize.Max)) {
                Box(
                    Modifier.width(ArcSizes.matrixFileColumn).fillMaxHeight().background(c.surfacePanel).gridRules(c.chartGrid).padding(StretchSpacing.space8),
                    contentAlignment = Alignment.BottomStart,
                ) {
                    BasicText(stringResource(Res.string.matrix_file).uppercase(), style = Stretch.type.eyebrow.merge(TextStyle(color = c.inkMuted)))
                }
                revisions.forEachIndexed { i, revision ->
                    MatrixHeaderCell(revision, checks[revision.head], i == revisions.lastIndex, i in picked, baseBranch, nowSeconds, Modifier.width(column))
                }
            }
            LazyColumn(Modifier.weight(1f), rememberLazyListState()) {
                items(rows, key = { it.path }) { row ->
                    MatrixRowView(row, column, selection?.takeIf { it.path == row.path }?.index) { onSelect(MatrixSelection(row.path, it)) }
                }
            }
        }
    }
}
