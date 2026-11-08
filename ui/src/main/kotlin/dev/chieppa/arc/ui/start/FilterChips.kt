package dev.chieppa.arc.ui.start

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.runtime.Composable
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.theme.StretchSpacing

/** A chip for each saved filter, with how many it found and how many are new, each opening the filter's tab. */
@Composable
fun FilterChips(workbench: Workbench) {
    val watch = workbench.watch
    val filters = watch.watchlist.filters
    if (filters.isEmpty()) return
    FlowRow(horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space6), verticalArrangement = Arrangement.spacedBy(StretchSpacing.space6)) {
        for (filter in filters) {
            FilterChip(filter.name, watch.results[filter.id]?.total, watch.newCount(filter.id)) {
                StartRunner.run(workbench, ArcStartActions.openFilter(filter.id))
            }
        }
    }
}
