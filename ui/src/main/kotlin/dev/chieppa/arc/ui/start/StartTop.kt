package dev.chieppa.arc.ui.start

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import dev.chieppa.arc.core.start.StartButton
import dev.chieppa.arc.core.start.StartScreenKind
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.theme.StretchSpacing

/**
 * The start screen's top area for [kind], with the main [buttons]: the open field and saved filters with nothing open;
 * the source's header, buttons and progress with one source; the sources' header and buttons with several.
 */
@Composable
fun StartTop(workbench: Workbench, kind: StartScreenKind, buttons: List<StartButton>) {
    val selection = workbench.state.selection
    when (kind) {
        StartScreenKind.NothingOpen -> Column(verticalArrangement = Arrangement.spacedBy(StretchSpacing.space12)) {
            OpenField(workbench)
            FilterChips(workbench)
            MainButtons(workbench, buttons)
        }
        StartScreenKind.OneSource -> {
            val source = selection.single()
            SourceHeader(workbench, source)
            Column(verticalArrangement = Arrangement.spacedBy(StretchSpacing.space10)) {
                MainButtons(workbench, buttons)
                StartReview.counts(workbench.state, source)?.takeIf { it.total > 0 }?.let { counts ->
                    ReviewBar(counts)
                    ReviewLegend(counts)
                }
            }
        }
        StartScreenKind.SeveralSources -> {
            SourcesHeader(workbench, selection)
            MainButtons(workbench, buttons)
        }
    }
}
