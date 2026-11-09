package dev.chieppa.arc.ui.runs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.chieppa.arc.core.actions.RunFilter
import dev.chieppa.arc.core.actions.RunNotify
import dev.chieppa.arc.core.pane.Page
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.filter_remove
import dev.chieppa.arc.design.generated.filter_rename
import dev.chieppa.arc.design.generated.filters_notify
import dev.chieppa.arc.design.generated.runs_footer
import dev.chieppa.arc.design.generated.runs_new_filter
import dev.chieppa.arc.design.generated.runs_new_filter_name
import dev.chieppa.arc.ui.pulls.WatchStatus
import dev.chieppa.arc.ui.util.ArcSizes
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.arc.ui.workbench.removeRunFilter
import dev.chieppa.stretch.frame.MenuItem
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchIcons
import dev.chieppa.stretch.theme.StretchSpacing
import dev.chieppa.stretch.ui.Button
import dev.chieppa.stretch.ui.ButtonKind
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

/**
 * The Runs drawer: every run filter with how many runs it shows, the focused tab's selected, opening its tab on a
 * click; a button for a new filter; the run notifications with their switches; and how often repos are polled, in the
 * error color while polling fails.
 */
@Composable
fun RunsDrawer(workbench: Workbench) {
    val c = Stretch.colors
    val watch = workbench.runs.watch
    val focused = workbench.state.paneLayout.activeTab?.let(workbench.state::page) as? Page.Runs
    var renaming by remember { mutableStateOf<RunFilter?>(null) }
    val newName = stringResource(Res.string.runs_new_filter_name)
    Column(Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth().padding(StretchSpacing.space6), verticalArrangement = Arrangement.spacedBy(StretchSpacing.space2)) {
            for (filter in watch.watchlist.filters) {
                RunFilterRow(filter, workbench, selected = focused?.filter == filter.id, onClick = { workbench.panes.openRuns(filter.id) }) { close ->
                    MenuItem(stringResource(Res.string.filter_rename), { close(); renaming = filter })
                    if (!filter.mine) MenuItem(stringResource(Res.string.filter_remove), { close(); workbench.removeRunFilter(filter.id) })
                }
            }
            Box { RenameRunFilterPopover(renaming, watch) { renaming = null } }
        }
        Box(Modifier.padding(horizontal = StretchSpacing.space12)) {
            Button(stringResource(Res.string.runs_new_filter), { workbench.panes.openRuns(watch.addFilter(newName, "").id) }, kind = ButtonKind.Quiet, icon = StretchIcons.Plus)
        }
        Column(
            Modifier.fillMaxWidth().padding(start = StretchSpacing.space14, end = StretchSpacing.space14, top = StretchSpacing.space10, bottom = StretchSpacing.space12),
            verticalArrangement = Arrangement.spacedBy(StretchSpacing.space2),
        ) {
            Row(Modifier.fillMaxWidth().height(ArcSizes.notifyHeader), verticalAlignment = Alignment.CenterVertically) {
                RunText(stringResource(Res.string.filters_notify), RunTextKind.Eyebrow, c.inkMuted, Modifier.weight(1f))
            }
            for (kind in RunNotify.entries) {
                RunNotifyRow(kind, kind in watch.watchlist.notify) { watch.watchlist = watch.watchlist.withNotify(kind, it) }
            }
            val failing = watch.status == WatchStatus.Failed || watch.status == WatchStatus.Unavailable
            RunText(
                pluralStringResource(Res.plurals.runs_footer, watch.activeRepos, watch.activeRepos, watch.quietRepos),
                RunTextKind.Small,
                if (failing) c.errorInk else c.inkSubtle,
                Modifier.padding(top = ArcSizes.drawerFooter),
                maxLines = 2,
            )
        }
    }
}
