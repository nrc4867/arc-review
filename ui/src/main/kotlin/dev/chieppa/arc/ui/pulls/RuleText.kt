package dev.chieppa.arc.ui.pulls

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
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
import org.jetbrains.compose.resources.stringResource

/** How a notification rule shows: its [icon], a [title] saying what it watches and its [scope], where. */
data class RuleText(val icon: ImageVector, val title: String, val scope: String)

/** How [rule] shows, naming its saved filter from [watchlist]. */
@Composable
fun ruleText(rule: NotifyRule, watchlist: Watchlist): RuleText = when (rule) {
    is NotifyRule.CommentsOnMyPullRequests -> RuleText(ArcIcons.Comment, stringResource(Res.string.rule_comments), stringResource(Res.string.rule_any_repo))
    is NotifyRule.Mentions -> RuleText(ArcIcons.At, stringResource(Res.string.rule_mentions), stringResource(Res.string.rule_any_repo))
    is NotifyRule.NewInFilter -> RuleText(
        ArcIcons.Filter,
        stringResource(Res.string.rule_new_in_filter, watchlist.filter(rule.filter)?.name.orEmpty()),
        stringResource(Res.string.rule_saved_filter),
    )
    is NotifyRule.NewLabelled -> RuleText(
        ArcIcons.Label,
        stringResource(Res.string.rule_new_labelled, rule.label),
        rule.repos.joinToString(REPO_SEPARATOR).ifEmpty { stringResource(Res.string.rule_any_repo) },
    )
}

private const val REPO_SEPARATOR = ", "
