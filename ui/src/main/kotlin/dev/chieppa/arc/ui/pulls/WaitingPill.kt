package dev.chieppa.arc.ui.pulls

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import dev.chieppa.arc.design.ArcIcons
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.waiting_author
import dev.chieppa.arc.design.generated.waiting_author_threads
import dev.chieppa.arc.design.generated.waiting_ci
import dev.chieppa.arc.design.generated.waiting_ci_check
import dev.chieppa.arc.design.generated.waiting_merge
import dev.chieppa.arc.design.generated.waiting_reviewers
import dev.chieppa.arc.design.generated.waiting_you
import dev.chieppa.arc.design.generated.waiting_you_again
import dev.chieppa.arc.host.github.pulls.WaitingOn
import dev.chieppa.arc.ui.util.ArcSizes
import dev.chieppa.stretch.theme.Icon
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchIcons
import dev.chieppa.stretch.theme.StretchSpacing
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

/** Who a pull request waits on, as a pill: the person in the on colors, failed checks in the error colors, others plain. */
@Composable
fun WaitingPill(waiting: WaitingOn, modifier: Modifier = Modifier) {
    val c = Stretch.colors
    val (fill, edge, ink) = when (waiting) {
        is WaitingOn.You -> Triple(c.onFill, c.chipOnEdge, c.onInk)
        is WaitingOn.Ci -> Triple(c.errorWash, c.errorEdge, c.errorInk)
        else -> Triple(Color.Transparent, c.chipEdge, c.inkControl)
    }
    val icon = when (waiting) {
        is WaitingOn.You -> StretchIcons.Eye
        is WaitingOn.Ci -> ArcIcons.CheckFail
        is WaitingOn.Author -> StretchIcons.Pen
        WaitingOn.Merge -> ArcIcons.Merge
        is WaitingOn.Reviewers -> ArcIcons.People
    }
    val text = when (waiting) {
        is WaitingOn.You -> stringResource(if (waiting.again) Res.string.waiting_you_again else Res.string.waiting_you)
        is WaitingOn.Ci -> waiting.check?.let { stringResource(Res.string.waiting_ci_check, it) } ?: stringResource(Res.string.waiting_ci)
        is WaitingOn.Author ->
            if (waiting.threads > 0) pluralStringResource(Res.plurals.waiting_author_threads, waiting.threads, waiting.threads) else stringResource(Res.string.waiting_author)
        WaitingOn.Merge -> stringResource(Res.string.waiting_merge)
        is WaitingOn.Reviewers -> pluralStringResource(Res.plurals.waiting_reviewers, waiting.count, waiting.count)
    }
    val shape = Stretch.shapes.pillShape
    Row(
        modifier.height(ArcSizes.waitingPill).clip(shape).background(fill).border(Stretch.stroke, edge, shape).padding(horizontal = StretchSpacing.space8),
        horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space4),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, ink, size = ArcSizes.smallIcon)
        BasicText(text, style = Stretch.type.chip.merge(TextStyle(color = ink)), maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}
