package dev.chieppa.arc.ui.start

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.matrix_count_separator
import dev.chieppa.arc.design.generated.start_files_reviewed
import dev.chieppa.arc.design.generated.start_list_pair
import dev.chieppa.arc.design.generated.start_sources_selected
import dev.chieppa.arc.design.generated.start_staged_across
import dev.chieppa.arc.ui.switcher.SeparatorDot
import dev.chieppa.arc.ui.util.SourceLabels
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchIcons
import dev.chieppa.stretch.theme.StretchSpacing
import dev.chieppa.stretch.ui.ControlLabel
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

/**
 * The several-source screen's header: how many sources are selected, their refs, how many of their files are reviewed
 * once they are read, and how many comments are staged across them.
 */
@Composable
fun SourcesHeader(workbench: Workbench, sources: List<Source>) {
    val c = Stretch.colors
    val state = workbench.state
    val refs = sources.map { state.pulls[it]?.key ?: SourceLabels.of(it) }
    val progress = sources.mapNotNull(state::progress)
    val staged = sources.sumOf { workbench.comments.controller.local.staged(it).size }
    val listed = if (refs.size < 2) refs.joinToString() else stringResource(Res.string.start_list_pair, refs.dropLast(1).joinToString(stringResource(Res.string.matrix_count_separator)), refs.last())
    Column(verticalArrangement = Arrangement.spacedBy(StretchSpacing.space8)) {
        StartTitle(StretchIcons.SplitView, stringResource(Res.string.start_sources_selected, sources.size))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space8), itemVerticalAlignment = Alignment.CenterVertically) {
            ControlLabel(listed, Stretch.type.valueSm, c.inkStrong)
            if (progress.size == sources.size) {
                SeparatorDot()
                ControlLabel(stringResource(Res.string.start_files_reviewed, progress.sumOf { it.reviewed }, progress.sumOf { it.total }), Stretch.type.bodySm, c.inkMuted)
            }
            if (staged > 0) {
                SeparatorDot()
                ControlLabel(pluralStringResource(Res.plurals.start_staged_across, staged, staged), Stretch.type.bodySm, c.inkMuted)
            }
        }
    }
}
