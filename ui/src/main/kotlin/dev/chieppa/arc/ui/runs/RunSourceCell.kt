package dev.chieppa.arc.ui.runs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.chieppa.arc.core.actions.WorkflowRun
import dev.chieppa.arc.design.ArcIcons
import dev.chieppa.arc.ui.util.ArcSizes
import dev.chieppa.stretch.theme.Icon
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchSpacing

/** A run's repo over the branch, pull request or tag it ran on. */
@Composable
fun RunSourceCell(run: WorkflowRun, modifier: Modifier = Modifier) {
    val c = Stretch.colors
    Column(modifier) {
        RunText(run.repo, RunTextKind.MonoSmall, c.inkControl)
        Row(horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space4), verticalAlignment = Alignment.CenterVertically) {
            val icon = when {
                isTag(run) -> ArcIcons.Tag
                run.fromFork -> ArcIcons.Merge
                run.pulls.isNotEmpty() -> ArcIcons.PullRequest
                else -> ArcIcons.Branch
            }
            Icon(icon, c.inkMuted, size = ArcSizes.smallIcon)
            RunText(runRefText(run), RunTextKind.MonoSmall, c.inkMuted)
        }
    }
}
