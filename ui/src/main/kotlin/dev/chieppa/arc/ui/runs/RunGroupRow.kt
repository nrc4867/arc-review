package dev.chieppa.arc.ui.runs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import dev.chieppa.arc.core.actions.RunGroup
import dev.chieppa.arc.core.actions.RunState
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.runs_group_done
import dev.chieppa.arc.design.generated.runs_group_failed
import dev.chieppa.arc.design.generated.runs_group_running
import dev.chieppa.arc.design.generated.runs_group_waiting
import dev.chieppa.arc.ui.util.ArcSizes
import dev.chieppa.arc.ui.util.bottomRule
import dev.chieppa.stretch.theme.Icon
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchIcons
import dev.chieppa.stretch.theme.StretchSpacing
import org.jetbrains.compose.resources.stringResource

/** A group's head in a run list: a chevron that folds it, the group's state icon and name, and how many it holds. */
@Composable
fun RunGroupRow(group: RunGroup, count: Int, folded: Boolean, onFold: () -> Unit) {
    val c = Stretch.colors
    Row(
        Modifier.fillMaxWidth().height(RunSizes.group).background(c.surfaceCanvas).bottomRule(c.linePanel)
            .clickable(role = Role.Button, onClick = onFold).padding(horizontal = StretchSpacing.space12),
        horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space8),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(if (folded) StretchIcons.ChevronRight else StretchIcons.ChevronDown, c.inkMuted, size = ArcSizes.smallIcon)
        val (state, name) = when (group) {
            RunGroup.WaitingOnYou -> RunState.Waiting to Res.string.runs_group_waiting
            RunGroup.Running -> RunState.Running to Res.string.runs_group_running
            RunGroup.Failed -> RunState.Failed to Res.string.runs_group_failed
            RunGroup.Done -> RunState.Succeeded to Res.string.runs_group_done
        }
        RunStateIcon(state, size = ArcSizes.checkMark)
        RunText(stringResource(name), RunTextKind.Strong, c.inkStrong)
        RunText(count.toString(), RunTextKind.MonoSmall, c.inkMuted)
    }
}
