package dev.chieppa.arc.ui.revisions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import dev.chieppa.arc.core.revision.CellKind
import dev.chieppa.arc.core.revision.MatrixRow
import dev.chieppa.arc.design.ArcIcons
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.key_enter
import dev.chieppa.arc.design.generated.matrix_changed_in
import dev.chieppa.arc.design.generated.matrix_changed_since
import dev.chieppa.arc.design.generated.matrix_count_addressed
import dev.chieppa.arc.design.generated.matrix_count_open
import dev.chieppa.arc.design.generated.matrix_count_resolved
import dev.chieppa.arc.design.generated.matrix_count_separator
import dev.chieppa.arc.design.generated.matrix_open_at
import dev.chieppa.arc.design.generated.matrix_open_diff
import dev.chieppa.arc.design.generated.matrix_open_diff_base
import dev.chieppa.arc.design.generated.matrix_selected
import dev.chieppa.arc.design.generated.matrix_selected_cell
import dev.chieppa.arc.design.generated.matrix_unchanged
import dev.chieppa.arc.design.generated.matrix_unchanged_since
import dev.chieppa.arc.design.generated.revisions_separator
import dev.chieppa.arc.ui.util.topRule
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchIcons
import dev.chieppa.stretch.theme.StretchSpacing
import dev.chieppa.stretch.ui.Button
import dev.chieppa.stretch.ui.ButtonKind
import dev.chieppa.stretch.ui.Kbd
import org.jetbrains.compose.resources.stringResource

/**
 * The line under the revision matrix about its selected cell: the file and revision, what happened to the file there
 * and its threads, and buttons that open its change from the revision it was last reviewed or changed at, and the
 * file whole at the revision (Enter).
 */
@Composable
fun SelectionLine(row: MatrixRow, index: Int, onOpenDiff: (from: Int?) -> Unit, onOpenAt: () -> Unit) {
    val c = Stretch.colors
    val small = Stretch.type.bodySm.merge(TextStyle(color = c.inkMuted))
    val cell = row.cells[index]
    val from = row.diffFrom(index)
    val number = index + 1
    val state = when {
        cell.kind != CellKind.Unchanged && from != null -> stringResource(Res.string.matrix_changed_since, from + 1)
        cell.kind != CellKind.Unchanged -> stringResource(Res.string.matrix_changed_in, number)
        else -> row.lastChangeBefore(index)?.let { stringResource(Res.string.matrix_unchanged_since, it + 1) } ?: stringResource(Res.string.matrix_unchanged)
    }
    val counts = listOfNotNull(
        cell.threads.open.takeIf { it > 0 }?.let { stringResource(Res.string.matrix_count_open, it) },
        cell.threads.addressed.takeIf { it > 0 }?.let { stringResource(Res.string.matrix_count_addressed, it) },
        cell.threads.resolved.takeIf { it > 0 }?.let { stringResource(Res.string.matrix_count_resolved, it) },
    ).joinToString(stringResource(Res.string.matrix_count_separator))
    val detail = if (counts.isEmpty()) state else state + stringResource(Res.string.revisions_separator) + counts
    Row(
        Modifier.fillMaxWidth().topRule(c.linePanel).background(c.surfaceCanvas)
            .padding(horizontal = StretchSpacing.space14, vertical = StretchSpacing.space8),
        horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space10),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BasicText(stringResource(Res.string.matrix_selected), style = small)
        BasicText(
            stringResource(Res.string.matrix_selected_cell, row.path.substringAfterLast('/'), number),
            style = Stretch.type.value.merge(TextStyle(color = c.inkStrong)),
            maxLines = 1,
        )
        BasicText(detail, Modifier.weight(1f, fill = false), style = small, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.weight(1f))
        val diff = if (from != null) stringResource(Res.string.matrix_open_diff, from + 1, number) else stringResource(Res.string.matrix_open_diff_base, number)
        Button(diff, { onOpenDiff(from) }, kind = ButtonKind.Quiet, icon = ArcIcons.Compare)
        Button(stringResource(Res.string.matrix_open_at, number), onOpenAt, icon = StretchIcons.File)
        Kbd(listOf(stringResource(Res.string.key_enter)))
    }
}
