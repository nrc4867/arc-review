package dev.chieppa.arc.ui.start

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.design.ArcIcons
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.sources_progress
import dev.chieppa.arc.ui.actions.ArcActionIds
import dev.chieppa.arc.ui.pulls.CheckMark
import dev.chieppa.arc.ui.util.ArcSizes
import dev.chieppa.arc.ui.util.SourceLabels
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.theme.Icon
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchSpacing
import dev.chieppa.stretch.ui.Button
import dev.chieppa.stretch.ui.ControlLabel
import org.jetbrains.compose.resources.stringResource

/**
 * One source in the Progress table: its title and ref, its progress bar, how many files changed since they were
 * reviewed, what waits on the person, the comments staged, its checks, and Continue with its next file to review.
 */
@Composable
fun ProgressTableRow(workbench: Workbench, source: Source) {
    val c = Stretch.colors
    val state = workbench.state
    val pull = state.pulls[source]
    val counts = StartReview.counts(state, source)
    val next = StartReview.nextFile(state, source)
    val count = Modifier.width(StartSizes.tableCount)
    Row(
        Modifier.fillMaxWidth().heightIn(min = StartSizes.tableRow).padding(horizontal = StretchSpacing.space12),
        horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space8),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            ControlLabel(pull?.title ?: state.snapshot(source)?.title ?: SourceLabels.of(source), Stretch.type.body, c.inkStrong)
            ControlLabel(pull?.key ?: SourceLabels.of(source), Stretch.type.valueSm, c.inkMuted)
        }
        Column(Modifier.width(StartSizes.tableBar), verticalArrangement = Arrangement.spacedBy(StretchSpacing.space4)) {
            counts?.let {
                ReviewBar(it)
                ControlLabel(stringResource(Res.string.sources_progress, it.reviewed, it.counted), Stretch.type.valueSm, c.inkMuted)
            }
        }
        Row(count, horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space2), verticalAlignment = Alignment.CenterVertically) {
            if ((counts?.changed ?: 0) > 0) {
                Icon(ArcIcons.ChangedSince, c.warnInk, size = ArcSizes.smallIcon)
                ControlLabel(counts?.changed.toString(), Stretch.type.valueSm, c.warnInk)
            }
        }
        ControlLabel(waitingRows(workbench, source).size.toString(), Stretch.type.valueSm, c.ink, count)
        ControlLabel(workbench.comments.controller.local.staged(source).size.toString(), Stretch.type.valueSm, c.ink, count)
        Box(count) { pull?.checks?.state?.let { CheckMark(it) } }
        Box(Modifier.width(StartSizes.tableAction), contentAlignment = Alignment.CenterEnd) {
            next?.let { path ->
                Button(StartReview.name(path), { StartRunner.run(workbench, ArcStartActions.on(ArcActionIds.CONTINUE_REVIEW, source)) }, icon = ArcIcons.Continue)
            }
        }
    }
}
