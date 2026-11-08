package dev.chieppa.arc.ui.start

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.start_base_head
import dev.chieppa.arc.host.github.pulls.CheckState
import dev.chieppa.arc.host.github.pulls.WaitingOn
import dev.chieppa.arc.host.github.pulls.WaitingOnRule
import dev.chieppa.arc.ui.pulls.SmallAvatar
import dev.chieppa.arc.ui.pulls.WaitingPill
import dev.chieppa.arc.ui.sources.sourceIcon
import dev.chieppa.arc.ui.switcher.SeparatorDot
import dev.chieppa.arc.ui.util.SourceLabels
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchSpacing
import dev.chieppa.stretch.ui.ControlLabel
import org.jetbrains.compose.resources.stringResource

/**
 * The one-source screen's header: the source's title, then its ref, author, base and head, its commits and how many
 * came since the person's review, and for a pull request who it waits on, and failed checks.
 */
@Composable
fun SourceHeader(workbench: Workbench, source: Source) {
    val c = Stretch.colors
    val state = workbench.state
    val pull = state.pulls[source]
    val snapshot = state.snapshot(source)
    val branch = source as? Source.BranchAgainstBase
    val base = pull?.baseBranch ?: branch?.base?.value
    val head = pull?.headBranch ?: branch?.branch?.value
    val waiting = pull?.let { WaitingOnRule.of(it, workbench.watch.viewer) }
    val failing = pull?.takeIf { it.checks.state == CheckState.Failed && waiting !is WaitingOn.Ci }?.let { WaitingOn.Ci(it.checks.failed.firstOrNull()) }
    val parts = listOfNotNull<@Composable () -> Unit>(
        { ControlLabel(pull?.key ?: SourceLabels.of(source), Stretch.type.valueSm, c.inkStrong) },
        pull?.author?.let { author ->
            {
                Row(horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space6), verticalAlignment = Alignment.CenterVertically) {
                    SmallAvatar(author)
                    ControlLabel(author, Stretch.type.bodySm, c.inkMuted)
                }
            }
        },
        if (base == null || head == null) null else { { ControlLabel(stringResource(Res.string.start_base_head, base, head), Stretch.type.valueSm, c.inkMuted) } },
        snapshot?.let { { ControlLabel(commitsText(state, source, it), Stretch.type.bodySm, c.inkMuted) } },
    )
    Column(verticalArrangement = Arrangement.spacedBy(StretchSpacing.space8)) {
        StartTitle(sourceIcon(source), pull?.title ?: snapshot?.title ?: SourceLabels.of(source))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space8),
            verticalArrangement = Arrangement.spacedBy(StretchSpacing.space4),
            itemVerticalAlignment = Alignment.CenterVertically,
        ) {
            parts.forEachIndexed { i, part ->
                if (i > 0) SeparatorDot()
                part()
            }
            waiting?.let { WaitingPill(it) }
            failing?.let { WaitingPill(it) }
        }
    }
}
