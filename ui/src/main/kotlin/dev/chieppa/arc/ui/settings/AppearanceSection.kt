package dev.chieppa.arc.ui.settings

import androidx.compose.runtime.Composable
import dev.chieppa.arc.core.settings.SettingsCard
import dev.chieppa.arc.core.settings.SettingsChoiceStyle
import dev.chieppa.arc.core.settings.SettingsControl
import dev.chieppa.arc.core.settings.SettingsEntry
import dev.chieppa.arc.core.settings.SettingsOption
import dev.chieppa.arc.core.settings.SettingsSection
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.settings_card_look
import dev.chieppa.arc.design.generated.settings_keywords_size
import dev.chieppa.arc.design.generated.settings_keywords_theme
import dev.chieppa.arc.design.generated.settings_section_appearance
import dev.chieppa.arc.design.generated.settings_size_interface
import dev.chieppa.arc.design.generated.settings_size_percent
import dev.chieppa.arc.design.generated.settings_theme
import org.jetbrains.compose.resources.stringResource

/** Arc's Appearance section: the theme and the interface size. */
@Composable
fun appearanceSection(preferences: UiPreferences): SettingsSection {
    val sizes = InterfaceSizes.all.map { SettingsOption(InterfaceSizes.id(it), stringResource(Res.string.settings_size_percent, InterfaceSizes.percent(it))) }
    return SettingsSection(
        ArcSettingIds.APPEARANCE,
        stringResource(Res.string.settings_section_appearance),
        SettingsIcons.THEME,
        ArcSettingIds.APPEARANCE_ORDER,
        listOf(
            SettingsCard(
                ArcSettingIds.LOOK_CARD,
                stringResource(Res.string.settings_card_look),
                SettingsIcons.PALETTE,
                entries = listOf(
                    SettingsEntry(
                        ArcSettingIds.THEME,
                        stringResource(Res.string.settings_theme),
                        SettingsControl.Choice(ThemeChoices.all.map { SettingsOption(it.id, it.name) }, preferences.theme.id),
                        icon = SettingsIcons.PALETTE,
                        keywords = settingsKeywords(Res.string.settings_keywords_theme),
                    ),
                    SettingsEntry(
                        ArcSettingIds.SIZE,
                        stringResource(Res.string.settings_size_interface),
                        SettingsControl.Choice(sizes, InterfaceSizes.id(preferences.interfaceSize), SettingsChoiceStyle.Segments),
                        icon = SettingsIcons.FIT,
                        keywords = settingsKeywords(Res.string.settings_keywords_size),
                    ),
                ),
            ),
        ),
    )
}
