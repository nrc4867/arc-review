package dev.chieppa.arc.ui.runs

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import dev.chieppa.arc.core.actions.WhyMine
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.why_assigned_pull
import dev.chieppa.arc.design.generated.why_my_pull
import dev.chieppa.arc.design.generated.why_started_by_me
import dev.chieppa.arc.design.generated.why_waiting_on_me
import dev.chieppa.arc.design.generated.why_watched
import dev.chieppa.stretch.theme.Icon
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchSpacing
import org.jetbrains.compose.resources.stringResource

/** Why a run is the person's, as a small pill: in the on colors when they started it, in the warning ones when it waits on them. */
@Composable
fun WhyChip(why: WhyMine) {
    WhyPill(RunIcons.why(why), stringResource(whyText(why)), whyTone(why))
}

/** A pill in the look of [WhyChip], for another fact about a run such as a stall. */
@Composable
fun WhyPill(icon: ImageVector, text: String, tone: WhyTone) {
    val c = Stretch.colors
    val (edge, ink, wash) = when (tone) {
        WhyTone.Me -> Triple(c.onEdge, c.onInk, Color.Transparent)
        WhyTone.Warn -> Triple(c.warnEdge, c.warnInk, c.warnWash)
        WhyTone.Plain -> Triple(c.chipEdge, c.inkControl, Color.Transparent)
    }
    val shape = Stretch.shapes.pillShape
    Row(
        Modifier.height(RunSizes.whyChip).background(wash, shape).border(Stretch.stroke, edge, shape).padding(horizontal = StretchSpacing.space6),
        horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space4),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, if (tone == WhyTone.Plain) c.inkMuted else ink, size = RunSizes.whyIcon)
        BasicText(text, style = Stretch.type.chip.merge(TextStyle(color = ink)), maxLines = 1)
    }
}

/** The colors of a [WhyPill]. */
enum class WhyTone { Me, Warn, Plain }

fun whyText(why: WhyMine) = when (why) {
    WhyMine.WaitingOnMe -> Res.string.why_waiting_on_me
    WhyMine.StartedByMe -> Res.string.why_started_by_me
    WhyMine.MyPull -> Res.string.why_my_pull
    WhyMine.AssignedPull -> Res.string.why_assigned_pull
    WhyMine.Watched -> Res.string.why_watched
}

fun whyTone(why: WhyMine) = when (why) {
    WhyMine.WaitingOnMe -> WhyTone.Warn
    WhyMine.StartedByMe -> WhyTone.Me
    else -> WhyTone.Plain
}
