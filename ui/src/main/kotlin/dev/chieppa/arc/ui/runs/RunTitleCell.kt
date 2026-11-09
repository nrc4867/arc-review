package dev.chieppa.arc.ui.runs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.chieppa.arc.core.actions.WorkflowRun
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.run_by_you
import dev.chieppa.arc.design.generated.run_event_tag
import dev.chieppa.arc.ui.pulls.SmallAvatar
import dev.chieppa.arc.ui.util.ArcSizes
import dev.chieppa.stretch.theme.Icon
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchSpacing
import org.jetbrains.compose.resources.stringResource

/** A run's workflow and title over what started it and who: `push · JP jpark`, with `you` for the person. */
@Composable
fun RunTitleCell(run: WorkflowRun, login: String?, modifier: Modifier = Modifier) {
    val c = Stretch.colors
    Column(modifier) {
        Row(horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space6), verticalAlignment = Alignment.CenterVertically) {
            RunText(run.workflowName, RunTextKind.Strong, c.inkStrong, Modifier.weight(1f, fill = false))
            RunText(run.title, RunTextKind.Small, c.inkSubtle, Modifier.weight(1f, fill = false))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space4), verticalAlignment = Alignment.CenterVertically) {
            val tag = isTag(run)
            Icon(RunIcons.trigger(run.event, tag), c.inkMuted, size = ArcSizes.smallIcon)
            RunText(if (tag) stringResource(Res.string.run_event_tag) else run.event, RunTextKind.Small, c.inkMuted)
            val actor = run.triggeringActor ?: run.actor
            if (actor != null && run.event != SCHEDULE) {
                Box(Modifier.size(ArcSizes.separatorDot).background(c.inkSubtle, CircleShape))
                SmallAvatar(actor)
                RunText(if (actor.equals(login, true)) stringResource(Res.string.run_by_you) else actor, RunTextKind.Small, c.inkMuted)
            }
        }
    }
}

private const val SCHEDULE = "schedule"
