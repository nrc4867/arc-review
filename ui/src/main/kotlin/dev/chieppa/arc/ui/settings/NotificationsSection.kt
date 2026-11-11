package dev.chieppa.arc.ui.settings

import androidx.compose.runtime.Composable
import dev.chieppa.arc.core.actions.RunNotify
import dev.chieppa.arc.core.actions.RunWatchlist
import dev.chieppa.arc.core.settings.SettingsCard
import dev.chieppa.arc.core.settings.SettingsColumn
import dev.chieppa.arc.core.settings.SettingsControl
import dev.chieppa.arc.core.settings.SettingsEntry
import dev.chieppa.arc.core.settings.SettingsSection
import dev.chieppa.arc.core.watch.NotifyRule
import dev.chieppa.arc.core.watch.Watchlist
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.settings_card_pull_requests
import dev.chieppa.arc.design.generated.settings_card_runs
import dev.chieppa.arc.design.generated.settings_keywords_notify
import dev.chieppa.arc.design.generated.settings_section_notifications
import dev.chieppa.arc.ui.pulls.ruleText
import dev.chieppa.arc.ui.runs.RunNotifyText
import org.jetbrains.compose.resources.stringResource

/**
 * Arc's Notifications section: a switch for each of the pull request notification rules in [pulls], and for each
 * kind of run notification in [runs].
 */
@Composable
fun notificationsSection(pulls: Watchlist, runs: RunWatchlist): SettingsSection {
    val keywords = settingsKeywords(Res.string.settings_keywords_notify)
    val rules = pulls.rules.map { rule ->
        val text = ruleText(rule, pulls)
        SettingsEntry(ArcSettingIds.pullRule(rule.id), text.title, SettingsControl.Switch(rule.on), text.scope, iconOf(rule), keywords)
    }
    val kinds = RunNotify.entries.map { kind ->
        val text = RunNotifyText.of(kind)
        SettingsEntry(ArcSettingIds.runNotify(kind), stringResource(text.title), SettingsControl.Switch(kind in runs.notify), stringResource(text.sub), iconOf(kind), keywords)
    }
    return SettingsSection(
        ArcSettingIds.NOTIFICATIONS,
        stringResource(Res.string.settings_section_notifications),
        SettingsIcons.BELL,
        ArcSettingIds.NOTIFICATIONS_ORDER,
        listOf(
            SettingsCard(ArcSettingIds.PULLS_CARD, stringResource(Res.string.settings_card_pull_requests), SettingsIcons.PULL_REQUEST, entries = rules),
            SettingsCard(ArcSettingIds.RUNS_CARD, stringResource(Res.string.settings_card_runs), SettingsIcons.RUNS, SettingsColumn.Side, kinds),
        ),
    )
}

private fun iconOf(rule: NotifyRule): String = when (rule) {
    is NotifyRule.CommentsOnMyPullRequests -> SettingsIcons.COMMENT
    is NotifyRule.Mentions -> SettingsIcons.AT
    is NotifyRule.NewInFilter -> SettingsIcons.FILTER
    is NotifyRule.NewLabelled -> SettingsIcons.LABEL
}

private fun iconOf(kind: RunNotify): String = when (kind) {
    RunNotify.MineFailed -> SettingsIcons.CHECK_FAIL
    RunNotify.WaitsOnMe -> SettingsIcons.GATE
    RunNotify.WatchedFinished -> SettingsIcons.EYE
    RunNotify.MineSucceeded -> SettingsIcons.CHECK_RUN
    RunNotify.MineStalled -> SettingsIcons.CHECK_WAITING
}
