package dev.chieppa.arc.ui.start

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.start_legend_aside
import dev.chieppa.arc.design.generated.start_legend_changed
import dev.chieppa.arc.design.generated.start_legend_reviewed
import dev.chieppa.arc.design.generated.start_legend_to_go
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchSpacing
import dev.chieppa.stretch.ui.ControlLabel
import org.jetbrains.compose.resources.stringResource

/** What [ReviewBar]'s segments count, each that has any files, beside a swatch of its color. */
@Composable
fun ReviewLegend(counts: ReviewCounts) {
    val c = Stretch.colors
    val entries = listOf(
        Triple(counts.reviewed, c.accent, stringResource(Res.string.start_legend_reviewed, counts.reviewed)),
        Triple(counts.changed, c.warnEdge, stringResource(Res.string.start_legend_changed, counts.changed)),
        Triple(counts.aside, c.lineStrong, stringResource(Res.string.start_legend_aside, counts.aside)),
        Triple(counts.toGo, c.track, stringResource(Res.string.start_legend_to_go, counts.toGo)),
    )
    FlowRow(horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space14)) {
        for ((count, color, text) in entries) {
            if (count == 0) continue
            Row(horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space6), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(StartSizes.legendSwatch).background(color, RoundedCornerShape(StretchSpacing.space2)))
                ControlLabel(text, Stretch.type.bodySm, c.inkMuted)
            }
        }
    }
}
