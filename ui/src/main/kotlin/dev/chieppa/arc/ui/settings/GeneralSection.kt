package dev.chieppa.arc.ui.settings

import androidx.compose.runtime.Composable
import dev.chieppa.arc.core.settings.SettingsCard
import dev.chieppa.arc.core.settings.SettingsControl
import dev.chieppa.arc.core.settings.SettingsEntry
import dev.chieppa.arc.core.settings.SettingsSection
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.settings_card_panes
import dev.chieppa.arc.design.generated.settings_keywords_line_highlight
import dev.chieppa.arc.design.generated.settings_keywords_wrap
import dev.chieppa.arc.design.generated.settings_line_highlight
import dev.chieppa.arc.design.generated.settings_line_highlight_detail
import dev.chieppa.arc.design.generated.settings_section_general
import dev.chieppa.arc.design.generated.settings_wrap
import dev.chieppa.arc.design.generated.settings_wrap_detail
import org.jetbrains.compose.resources.stringResource

/**
 * Arc's General section: how panes show code, whether they wrap long lines and how changed lines are washed, and the
 * [updates] card where Arc updates itself, when it can.
 */
@Composable
fun generalSection(preferences: UiPreferences, updates: SettingsCard? = null): SettingsSection = SettingsSection(
    ArcSettingIds.GENERAL,
    stringResource(Res.string.settings_section_general),
    SettingsIcons.SETTINGS,
    ArcSettingIds.GENERAL_ORDER,
    listOfNotNull(
        SettingsCard(
            ArcSettingIds.PANES_CARD,
            stringResource(Res.string.settings_card_panes),
            SettingsIcons.SPLIT_VIEW,
            entries = listOf(
                SettingsEntry(
                    ArcSettingIds.WRAP,
                    stringResource(Res.string.settings_wrap),
                    SettingsControl.Switch(preferences.wrapLines),
                    stringResource(Res.string.settings_wrap_detail),
                    SettingsIcons.WRAP,
                    settingsKeywords(Res.string.settings_keywords_wrap),
                ),
                SettingsEntry(
                    ArcSettingIds.LINE_HIGHLIGHT,
                    stringResource(Res.string.settings_line_highlight),
                    SettingsControl.Switch(preferences.wholeLineHighlight),
                    stringResource(Res.string.settings_line_highlight_detail),
                    SettingsIcons.CHANGED_SINCE,
                    settingsKeywords(Res.string.settings_keywords_line_highlight),
                ),
            ),
        ),
        updates,
    ),
)
