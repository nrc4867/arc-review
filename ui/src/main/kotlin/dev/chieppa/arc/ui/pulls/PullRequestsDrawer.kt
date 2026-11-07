package dev.chieppa.arc.ui.pulls

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import dev.chieppa.arc.core.pane.Page
import dev.chieppa.arc.core.watch.SavedFilter
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.filter_remove
import dev.chieppa.arc.design.generated.filter_rename
import dev.chieppa.arc.design.generated.filters_add_rule
import dev.chieppa.arc.design.generated.filters_footer
import dev.chieppa.arc.design.generated.filters_notify
import dev.chieppa.arc.design.generated.rule_remove
import dev.chieppa.arc.ui.util.ArcSizes
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.frame.MenuItem
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchIcons
import dev.chieppa.stretch.theme.StretchSizes
import dev.chieppa.stretch.theme.StretchSpacing
import dev.chieppa.stretch.ui.IconButton
import dev.chieppa.stretch.ui.Tooltip
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

/**
 * The Pull requests drawer: every saved filter, the focused tab's selected, opening its tab on a click; making a filter
 * from a pasted search link; the notification rules with their switches; and how often GitHub is polled, in the error
 * color while polling fails.
 */
@Composable
fun PullRequestsDrawer(workbench: Workbench) {
    val c = Stretch.colors
    val watch = workbench.watch
    val watchlist = watch.watchlist
    val focusedPage = workbench.state.paneLayout.activeTab?.let(workbench.state::page) as? Page.Filter
    var renaming by remember { mutableStateOf<SavedFilter?>(null) }
    var addingRule by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth().padding(StretchSpacing.space6), verticalArrangement = Arrangement.spacedBy(StretchSpacing.space2)) {
            for (filter in watchlist.filters) {
                FilterListRow(
                    filter = filter,
                    newCount = watch.newCount(filter.id),
                    total = watch.results[filter.id]?.total,
                    selected = focusedPage?.filter == filter.id,
                    onClick = { workbench.panes.openFilter(filter.id) },
                ) { close ->
                    MenuItem(stringResource(Res.string.filter_rename), { close(); renaming = filter })
                    MenuItem(stringResource(Res.string.filter_remove), { close(); workbench.removeFilter(filter.id) })
                }
            }
            Box { RenameFilterPopover(renaming, watch) { renaming = null } }
        }
        NewFilterSection(watch) { workbench.panes.openFilter(it) }
        Column(
            Modifier.fillMaxWidth().padding(start = StretchSpacing.space14, end = StretchSpacing.space14, top = StretchSpacing.space10, bottom = StretchSpacing.space12),
            verticalArrangement = Arrangement.spacedBy(StretchSpacing.space2),
        ) {
            Row(Modifier.fillMaxWidth().height(ArcSizes.notifyHeader), verticalAlignment = Alignment.CenterVertically) {
                BasicText(stringResource(Res.string.filters_notify).uppercase(), Modifier.weight(1f), style = Stretch.type.eyebrow.merge(TextStyle(color = c.inkMuted)))
                Box {
                    val add = stringResource(Res.string.filters_add_rule)
                    Tooltip(add) { IconButton(StretchIcons.Plus, add, { addingRule = true }, size = StretchSizes.seg) }
                    AddRulePopover(addingRule, watch) { addingRule = false }
                }
            }
            for (rule in watchlist.rules) {
                RuleRow(rule, watchlist, { watch.putRule(rule.switched(it)) }) { close ->
                    MenuItem(stringResource(Res.string.rule_remove), { close(); watch.removeRule(rule.id) })
                }
            }
            val failing = watch.status == WatchStatus.Failed || watch.status == WatchStatus.Unavailable
            val live = watchlist.filters.size
            BasicText(
                pluralStringResource(Res.plurals.filters_footer, live, PullTimes.POLL_MINUTES, live),
                Modifier.padding(top = ArcSizes.drawerFooter),
                style = Stretch.type.chip.merge(TextStyle(color = if (failing) c.errorInk else c.inkSubtle, fontWeight = FontWeight.Normal)),
            )
        }
    }
}
