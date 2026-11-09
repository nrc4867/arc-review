package dev.chieppa.arc.ui.runs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import dev.chieppa.arc.core.actions.MineRule
import dev.chieppa.arc.design.ArcIcons
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.rule_mine_pulls
import dev.chieppa.arc.design.generated.rule_mine_started
import dev.chieppa.arc.design.generated.rule_mine_waiting
import dev.chieppa.arc.design.generated.rule_mine_watched
import dev.chieppa.stretch.theme.Icon
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchIcons
import dev.chieppa.stretch.theme.StretchSpacing
import org.jetbrains.compose.resources.stringResource

/** One of the Mine rules as a pill that switches it on or off, with how many runs it finds. */
@Composable
fun MineRuleToggle(rule: MineRule, on: Boolean, count: Int, onSwitch: (Boolean) -> Unit) {
    val c = Stretch.colors
    val shape = Stretch.shapes.pillShape
    val ink = if (on) c.onInk else c.inkControl
    Row(
        Modifier.height(RunSizes.ruleToggle).clip(shape).background(if (on) c.onFill else Color.Transparent)
            .border(Stretch.stroke, if (on) c.chipOnEdge else c.chipEdge, shape)
            .clickable(role = Role.Switch) { onSwitch(!on) }
            .semantics { selected = on }
            .padding(horizontal = StretchSpacing.space10),
        horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space6),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(mineRuleIcon(rule), ink, size = RunSizes.ruleIcon)
        RunText(stringResource(mineRuleText(rule)), RunTextKind.Small, ink)
        RunText(count.toString(), RunTextKind.MonoSmall, if (on) c.onInk else c.inkMuted)
    }
}

fun mineRuleIcon(rule: MineRule) = when (rule) {
    MineRule.StartedByMe -> ArcIcons.Person
    MineRule.OnMyPulls -> ArcIcons.PullRequest
    MineRule.WaitingOnMe -> ArcIcons.Gate
    MineRule.Watched -> StretchIcons.Eye
}

fun mineRuleText(rule: MineRule) = when (rule) {
    MineRule.StartedByMe -> Res.string.rule_mine_started
    MineRule.OnMyPulls -> Res.string.rule_mine_pulls
    MineRule.WaitingOnMe -> Res.string.rule_mine_waiting
    MineRule.Watched -> Res.string.rule_mine_watched
}
