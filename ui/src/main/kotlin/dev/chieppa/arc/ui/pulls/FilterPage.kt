package dev.chieppa.arc.ui.pulls

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import dev.chieppa.arc.core.pane.Page
import dev.chieppa.arc.core.watch.FilterGrouping
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.pull_ref
import dev.chieppa.arc.host.github.pulls.WaitingOnRule
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.ui.Divider
import org.jetbrains.compose.resources.stringResource

/**
 * A saved filter's tab: its tool row over its pull requests, grouped as the filter says, each group folding under its
 * head. While it shows, the filter counts as looked at, and what changed since it was last looked at stays marked.
 * A page whose filter is gone is plain.
 */
@Composable
fun FilterPage(page: Page.Filter, workbench: Workbench, modifier: Modifier = Modifier) {
    val watch = workbench.watch
    val filter = watch.watchlist.filter(page.filter) ?: return Box(modifier.fillMaxSize())
    DisposableEffect(filter.id) {
        watch.startLooking(filter.id)
        onDispose { watch.stopLooking(filter.id) }
    }
    val now = currentTime()
    var folded by remember(filter.id) { mutableStateOf(emptySet<String?>()) }
    val pulls = watch.results[filter.id]?.pulls.orEmpty()
    val groups = PullGroups.of(pulls, filter.grouping)
    Column(modifier.fillMaxSize()) {
        FilterToolRow(filter, watch, now)
        Divider()
        LazyColumn(Modifier.weight(1f).fillMaxWidth(), rememberLazyListState()) {
            item(HEADER) { PullHeaderRow() }
            for (group in groups) {
                val open = group.name !in folded
                if (filter.grouping != FilterGrouping.None) {
                    item(GROUP + group.name) {
                        PullGroupRow(group, !open) { folded = if (open) folded + group.name else folded - group.name }
                    }
                }
                if (open) {
                    items(group.pulls, key = { group.name + it.key }) { pull ->
                        val source = workbench.sourceOf(pull)
                        val holding = source?.let(workbench::reviewHolding)
                        PullRow(
                            pull = pull,
                            ref = stringResource(Res.string.pull_ref, source?.repo?.value ?: pull.repo.name, pull.number),
                            isNew = watch.isNew(filter.id, pull),
                            waiting = WaitingOnRule.of(pull, watch.viewer),
                            now = now,
                            holding = holding,
                            onAdd = source?.takeIf { holding == null }?.let { { workbench.addSource(it.ref) } },
                        )
                    }
                }
            }
        }
    }
}

private const val HEADER = "header"
private const val GROUP = "group:"
