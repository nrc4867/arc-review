package dev.chieppa.arc.ui.runs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import dev.chieppa.arc.core.actions.RunFilter
import dev.chieppa.arc.core.actions.RunGroup
import dev.chieppa.arc.core.actions.RunGroups
import dev.chieppa.arc.design.ArcIcons
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.runs_mine_sub
import dev.chieppa.arc.design.generated.runs_wait_count
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.frame.ContextMenuArea
import dev.chieppa.stretch.theme.Icon
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchSpacing
import dev.chieppa.stretch.ui.Chip
import dev.chieppa.stretch.ui.ChipKind
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

/**
 * A run filter in the Runs drawer: its name, with how many of its runs wait on the person, what it holds under it,
 * and how many runs it shows. The [selected] filter, the focused tab's, is in the on colors; a right-click opens [menu].
 */
@Composable
fun RunFilterRow(filter: RunFilter, workbench: Workbench, selected: Boolean, onClick: () -> Unit, menu: @Composable ColumnScope.(close: () -> Unit) -> Unit) {
    val c = Stretch.colors
    val watch = workbench.runs.watch
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    val shape = Stretch.shapes.rowShape
    val ink = if (selected) c.onInk else c.ink
    val runs = watch.runsOf(filter)
    val shown = RunGroups.of(runs, watch.waitingOnMe, watch.now() / RunTimes.MILLIS_PER_SECOND)
    val waiting = shown.firstOrNull { it.group == RunGroup.WaitingOnYou }?.runs?.size ?: 0
    ContextMenuArea(menu) {
        Row(
            Modifier.fillMaxWidth().clip(shape)
                .background(if (selected) c.onFill else if (hovered) c.surfaceControl else Color.Transparent)
                .border(Stretch.stroke, if (selected) c.onEdge else Color.Transparent, shape)
                .hoverable(interaction)
                .clickable(interaction, null, role = Role.Button, onClick = onClick)
                .semantics { this.selected = selected }
                .padding(horizontal = StretchSpacing.space8, vertical = StretchSpacing.space6),
            horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space8),
        ) {
            Icon(ArcIcons.Runs, ink, Modifier.padding(top = StretchSpacing.space2))
            Column(Modifier.weight(1f)) {
                Row(horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space6), verticalAlignment = Alignment.CenterVertically) {
                    RunText(filter.name, RunTextKind.Strong, ink, Modifier.weight(1f, fill = false))
                    if (filter.mine && waiting > 0) Chip(stringResource(Res.string.runs_wait_count, waiting), kind = ChipKind.On)
                }
                val sub = if (filter.mine) {
                    val followed = watch.followed.size
                    pluralStringResource(Res.plurals.runs_mine_sub, followed, watch.watchlist.mineRules.size, followed)
                } else {
                    filter.query
                }
                RunText(sub, RunTextKind.MonoSmall, if (selected) ink else c.inkSubtle)
            }
            RunText(shown.sumOf { it.runs.size }.toString(), RunTextKind.MonoSmall, ink)
        }
    }
}
