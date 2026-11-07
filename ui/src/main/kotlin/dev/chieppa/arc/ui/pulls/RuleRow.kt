package dev.chieppa.arc.ui.pulls

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import dev.chieppa.arc.core.watch.NotifyRule
import dev.chieppa.arc.core.watch.Watchlist
import dev.chieppa.arc.design.ArcIcons
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.rule_any_repo
import dev.chieppa.arc.design.generated.rule_comments
import dev.chieppa.arc.design.generated.rule_mentions
import dev.chieppa.arc.design.generated.rule_new_in_filter
import dev.chieppa.arc.design.generated.rule_new_labelled
import dev.chieppa.arc.design.generated.rule_saved_filter
import dev.chieppa.arc.ui.util.ArcSizes
import dev.chieppa.stretch.frame.ContextMenuArea
import dev.chieppa.stretch.theme.Icon
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchSpacing
import dev.chieppa.stretch.ui.Toggle
import org.jetbrains.compose.resources.stringResource

/** A notification rule in the Pull requests drawer: what it watches, where, and a switch that turns it on or off. */
@Composable
fun RuleRow(rule: NotifyRule, watchlist: Watchlist, onSwitch: (Boolean) -> Unit, menu: @Composable ColumnScope.(close: () -> Unit) -> Unit) {
    val c = Stretch.colors
    val (icon: ImageVector, title: String, scope: String) = when (rule) {
        is NotifyRule.CommentsOnMyPullRequests -> Triple(ArcIcons.Comment, stringResource(Res.string.rule_comments), stringResource(Res.string.rule_any_repo))
        is NotifyRule.Mentions -> Triple(ArcIcons.At, stringResource(Res.string.rule_mentions), stringResource(Res.string.rule_any_repo))
        is NotifyRule.NewInFilter -> Triple(
            ArcIcons.Filter,
            stringResource(Res.string.rule_new_in_filter, watchlist.filter(rule.filter)?.name.orEmpty()),
            stringResource(Res.string.rule_saved_filter),
        )
        is NotifyRule.NewLabelled -> Triple(
            ArcIcons.Label,
            stringResource(Res.string.rule_new_labelled, rule.label),
            rule.repos.joinToString(REPO_SEPARATOR).ifEmpty { stringResource(Res.string.rule_any_repo) },
        )
    }
    ContextMenuArea(menu) {
        Row(
            Modifier.fillMaxWidth().heightIn(min = ArcSizes.ruleRow),
            horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space10),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, c.inkControl)
            Column(Modifier.weight(1f)) {
                BasicText(title, style = Stretch.type.body.merge(TextStyle(color = c.ink)), maxLines = 1, overflow = TextOverflow.Ellipsis)
                BasicText(scope, style = Stretch.type.chip.merge(TextStyle(color = c.inkSubtle, fontWeight = FontWeight.Normal)), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Toggle(rule.on, onSwitch, title)
        }
    }
}

private const val REPO_SEPARATOR = ", "
