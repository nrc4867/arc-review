package dev.chieppa.arc.ui.start

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.start_column_changed
import dev.chieppa.arc.design.generated.start_column_checks
import dev.chieppa.arc.design.generated.start_column_reviewed
import dev.chieppa.arc.design.generated.start_column_source
import dev.chieppa.arc.design.generated.start_column_staged
import dev.chieppa.arc.design.generated.start_column_waiting
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchSpacing
import dev.chieppa.stretch.ui.ControlLabel
import dev.chieppa.stretch.ui.Divider
import org.jetbrains.compose.resources.stringResource

/** The Progress card's table: a row for each selected source, under a row naming the columns. */
@Composable
fun ProgressTable(workbench: Workbench) {
    val c = Stretch.colors
    val eyebrow = Stretch.type.eyebrow
    Row(
        Modifier.fillMaxWidth().padding(horizontal = StretchSpacing.space12, vertical = StretchSpacing.space8),
        horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space8),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ControlLabel(stringResource(Res.string.start_column_source).uppercase(), eyebrow, c.inkMuted, Modifier.weight(1f))
        ControlLabel(stringResource(Res.string.start_column_reviewed).uppercase(), eyebrow, c.inkMuted, Modifier.width(StartSizes.tableBar))
        for (column in listOf(Res.string.start_column_changed, Res.string.start_column_waiting, Res.string.start_column_staged, Res.string.start_column_checks)) {
            ControlLabel(stringResource(column).uppercase(), eyebrow, c.inkMuted, Modifier.width(StartSizes.tableCount))
        }
        Spacer(Modifier.width(StartSizes.tableAction))
    }
    for (source in workbench.state.selection) {
        Divider()
        ProgressTableRow(workbench, source)
    }
}
