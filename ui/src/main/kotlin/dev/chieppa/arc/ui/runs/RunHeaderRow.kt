package dev.chieppa.arc.ui.runs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.runs_col_jobs
import dev.chieppa.arc.design.generated.runs_col_run
import dev.chieppa.arc.design.generated.runs_col_source
import dev.chieppa.arc.design.generated.runs_col_time
import dev.chieppa.arc.design.generated.runs_col_why
import dev.chieppa.arc.ui.util.bottomRule
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchSpacing
import org.jetbrains.compose.resources.stringResource

/** The column heads over a run list, lined up with [RunRow]. */
@Composable
fun RunHeaderRow() {
    val c = Stretch.colors
    Row(
        Modifier.fillMaxWidth().heightIn(min = RunSizes.header).background(c.surfacePanel).bottomRule(c.chartGrid).padding(horizontal = StretchSpacing.space12),
        horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space12),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.width(RunSizes.stateColumn))
        RunText(stringResource(Res.string.runs_col_run), RunTextKind.Eyebrow, c.inkMuted, Modifier.weight(1f))
        RunText(stringResource(Res.string.runs_col_source), RunTextKind.Eyebrow, c.inkMuted, Modifier.width(RunSizes.sourceColumn))
        RunText(stringResource(Res.string.runs_col_why), RunTextKind.Eyebrow, c.inkMuted, Modifier.width(RunSizes.whyColumn))
        RunText(stringResource(Res.string.runs_col_jobs), RunTextKind.Eyebrow, c.inkMuted, Modifier.width(RunSizes.jobsColumn))
        RunText(stringResource(Res.string.runs_col_time), RunTextKind.Eyebrow, c.inkMuted, Modifier.width(RunSizes.timeColumn), TextAlign.End)
        Box(Modifier.width(RunSizes.actionsColumn))
    }
}
