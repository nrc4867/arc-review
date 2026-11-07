package dev.chieppa.arc.ui.status

import androidx.compose.runtime.Composable
import dev.chieppa.arc.core.watch.FilterGrouping
import dev.chieppa.arc.core.watch.SavedFilter
import dev.chieppa.arc.design.ArcIcons
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.status_budget
import dev.chieppa.arc.design.generated.status_labels
import dev.chieppa.arc.design.generated.status_open_pulls
import dev.chieppa.arc.design.generated.status_pair
import dev.chieppa.arc.design.generated.status_rules
import dev.chieppa.arc.ui.pulls.PullGroups
import dev.chieppa.arc.ui.pulls.PullWatch
import dev.chieppa.stretch.frame.StatusItem
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

/** The status bar's start for a saved filter's tab: the filter, how many pull requests and labels it shows, and the GitHub budget left. */
@Composable
fun FilterStatusStart(filter: SavedFilter, watch: PullWatch) {
    StatusItem(filter.name, ArcIcons.Filter)
    val result = watch.results[filter.id]
    if (result != null) {
        val labels = PullGroups.of(result.pulls, FilterGrouping.Label).count { it.label != null }
        StatusItem(
            stringResource(
                Res.string.status_pair,
                pluralStringResource(Res.plurals.status_open_pulls, result.total, result.total),
                pluralStringResource(Res.plurals.status_labels, labels, labels),
            ),
        )
    }
    watch.budget?.let { StatusItem(stringResource(Res.string.status_budget, it.remaining, it.limit)) }
}

/** The status bar's end for a saved filter's tab: how many notification rules there are and how many are on. */
@Composable
fun FilterStatusEnd(watch: PullWatch) {
    val rules = watch.watchlist.rules
    StatusItem(pluralStringResource(Res.plurals.status_rules, rules.size, rules.size, rules.count { it.on }), ArcIcons.Bell, onClick = null)
}
