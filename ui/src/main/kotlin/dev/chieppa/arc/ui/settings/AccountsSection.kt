package dev.chieppa.arc.ui.settings

import androidx.compose.runtime.Composable
import dev.chieppa.arc.core.settings.SettingsCard
import dev.chieppa.arc.core.settings.SettingsControl
import dev.chieppa.arc.core.settings.SettingsEntry
import dev.chieppa.arc.core.settings.SettingsSection
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.settings_account
import dev.chieppa.arc.design.generated.settings_account_login
import dev.chieppa.arc.design.generated.settings_account_signed_out
import dev.chieppa.arc.design.generated.settings_card_github
import dev.chieppa.arc.design.generated.settings_keywords_account
import dev.chieppa.arc.design.generated.settings_section_accounts
import dev.chieppa.arc.design.generated.settings_token
import dev.chieppa.arc.design.generated.settings_token_gh
import dev.chieppa.arc.design.generated.settings_token_other
import dev.chieppa.arc.host.github.auth.TokenOrigin
import dev.chieppa.arc.ui.pulls.PullWatch
import dev.chieppa.arc.ui.pulls.WatchStatus
import org.jetbrains.compose.resources.stringResource

/**
 * Arc's Accounts section, to read only: the GitHub account [watch] found and where its token came from. With no token
 * the account shows as not signed in, set apart; while the first poll is under way it is blank.
 */
@Composable
fun accountsSection(watch: PullWatch): SettingsSection {
    val keywords = settingsKeywords(Res.string.settings_keywords_account)
    val login = watch.viewer?.login
    val account = when {
        login != null -> SettingsControl.Readout(stringResource(Res.string.settings_account_login, login))
        watch.status == WatchStatus.Unavailable && watch.tokenOrigin == null -> SettingsControl.Readout(stringResource(Res.string.settings_account_signed_out), attention = true)
        else -> SettingsControl.Readout("")
    }
    val origin = when (val from = watch.tokenOrigin) {
        TokenOrigin.GhCli -> stringResource(Res.string.settings_token_gh)
        is TokenOrigin.Environment -> from.name
        TokenOrigin.Other -> stringResource(Res.string.settings_token_other)
        null -> null
    }
    val entries = listOfNotNull(
        SettingsEntry(ArcSettingIds.ACCOUNT, stringResource(Res.string.settings_account), account, keywords = keywords),
        origin?.let { SettingsEntry(ArcSettingIds.TOKEN, stringResource(Res.string.settings_token), SettingsControl.Readout(it), keywords = keywords) },
    )
    return SettingsSection(
        ArcSettingIds.ACCOUNTS,
        stringResource(Res.string.settings_section_accounts),
        SettingsIcons.PEOPLE,
        ArcSettingIds.ACCOUNTS_ORDER,
        listOf(SettingsCard(ArcSettingIds.GITHUB_CARD, stringResource(Res.string.settings_card_github), SettingsIcons.PULL_REQUEST, entries = entries)),
    )
}
