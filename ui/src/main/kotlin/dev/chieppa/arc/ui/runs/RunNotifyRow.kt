package dev.chieppa.arc.ui.runs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.chieppa.arc.core.actions.RunNotify
import dev.chieppa.arc.design.ArcIcons
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.notify_kind_failed
import dev.chieppa.arc.design.generated.notify_kind_failed_sub
import dev.chieppa.arc.design.generated.notify_kind_finished
import dev.chieppa.arc.design.generated.notify_kind_finished_sub
import dev.chieppa.arc.design.generated.notify_kind_stalled
import dev.chieppa.arc.design.generated.notify_kind_stalled_sub
import dev.chieppa.arc.design.generated.notify_kind_succeeded
import dev.chieppa.arc.design.generated.notify_kind_succeeded_sub
import dev.chieppa.arc.design.generated.notify_kind_waits
import dev.chieppa.arc.design.generated.notify_kind_waits_sub
import dev.chieppa.arc.ui.util.ArcSizes
import dev.chieppa.stretch.theme.Icon
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchIcons
import dev.chieppa.stretch.theme.StretchSpacing
import dev.chieppa.stretch.ui.Toggle
import org.jetbrains.compose.resources.stringResource

/** A run notification in the Runs drawer: what it tells of, about which runs, and a switch. */
@Composable
fun RunNotifyRow(kind: RunNotify, on: Boolean, onSwitch: (Boolean) -> Unit) {
    val c = Stretch.colors
    val (icon, title, sub) = when (kind) {
        RunNotify.MineFailed -> Triple(ArcIcons.CheckFail, Res.string.notify_kind_failed, Res.string.notify_kind_failed_sub)
        RunNotify.WaitsOnMe -> Triple(ArcIcons.Gate, Res.string.notify_kind_waits, Res.string.notify_kind_waits_sub)
        RunNotify.WatchedFinished -> Triple(StretchIcons.Eye, Res.string.notify_kind_finished, Res.string.notify_kind_finished_sub)
        RunNotify.MineSucceeded -> Triple(ArcIcons.CheckRun, Res.string.notify_kind_succeeded, Res.string.notify_kind_succeeded_sub)
        RunNotify.MineStalled -> Triple(ArcIcons.CheckWaiting, Res.string.notify_kind_stalled, Res.string.notify_kind_stalled_sub)
    }
    val label = stringResource(title)
    Row(
        Modifier.fillMaxWidth().heightIn(min = ArcSizes.ruleRow),
        horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space10),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, c.inkControl)
        Column(Modifier.weight(1f)) {
            RunText(label, RunTextKind.Body, c.ink)
            RunText(stringResource(sub), RunTextKind.Small, c.inkSubtle)
        }
        Toggle(on, onSwitch, label)
    }
}
