package dev.chieppa.arc.ui.runs

import androidx.compose.ui.graphics.vector.ImageVector
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
import dev.chieppa.stretch.theme.StretchIcons
import org.jetbrains.compose.resources.StringResource

/** How a run notification shows: its [icon], a [title] saying what it tells of and a [sub] saying about which runs. */
data class RunNotifyText(val icon: ImageVector, val title: StringResource, val sub: StringResource) {
    companion object {
        fun of(kind: RunNotify): RunNotifyText = when (kind) {
            RunNotify.MineFailed -> RunNotifyText(ArcIcons.CheckFail, Res.string.notify_kind_failed, Res.string.notify_kind_failed_sub)
            RunNotify.WaitsOnMe -> RunNotifyText(ArcIcons.Gate, Res.string.notify_kind_waits, Res.string.notify_kind_waits_sub)
            RunNotify.WatchedFinished -> RunNotifyText(StretchIcons.Eye, Res.string.notify_kind_finished, Res.string.notify_kind_finished_sub)
            RunNotify.MineSucceeded -> RunNotifyText(ArcIcons.CheckRun, Res.string.notify_kind_succeeded, Res.string.notify_kind_succeeded_sub)
            RunNotify.MineStalled -> RunNotifyText(ArcIcons.CheckWaiting, Res.string.notify_kind_stalled, Res.string.notify_kind_stalled_sub)
        }
    }
}
