package dev.chieppa.arc.ui.update

import androidx.compose.runtime.Composable
import dev.chieppa.arc.core.settings.SettingsCard
import dev.chieppa.arc.core.settings.SettingsControl
import dev.chieppa.arc.core.settings.SettingsEntry
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.settings_card_updates
import dev.chieppa.arc.design.generated.settings_keywords_updates
import dev.chieppa.arc.design.generated.settings_previous
import dev.chieppa.arc.design.generated.settings_switch_back
import dev.chieppa.arc.design.generated.settings_update
import dev.chieppa.arc.design.generated.settings_version
import dev.chieppa.arc.design.generated.update_downloading
import dev.chieppa.arc.design.generated.update_restart
import dev.chieppa.arc.design.generated.update_to
import dev.chieppa.arc.ui.settings.ArcSettingIds
import dev.chieppa.arc.ui.settings.SettingsIcons
import dev.chieppa.arc.ui.settings.settingsKeywords
import org.jetbrains.compose.resources.stringResource

/**
 * The Updates card of Settings › General, where Arc updates itself: the running version, a newer release's button
 * (update, downloading, restart; marked when it failed), and switching back to the previous version. None where Arc
 * cannot update itself.
 */
@Composable
fun updatesCard(updates: UpdateController): SettingsCard? {
    val version = updates.version ?: return null
    val keywords = settingsKeywords(Res.string.settings_keywords_updates)
    val update = when (val state = updates.state) {
        UpdateState.Current -> null
        is UpdateState.Available -> SettingsControl.Action(stringResource(Res.string.update_to, state.version))
        is UpdateState.Failed -> SettingsControl.Action(stringResource(Res.string.update_to, state.version), attention = true)
        is UpdateState.Downloading -> SettingsControl.Action(stringResource(Res.string.update_downloading, state.version), enabled = false)
        is UpdateState.Ready -> SettingsControl.Action(stringResource(Res.string.update_restart))
    }
    val previous = updates.previous
    return SettingsCard(
        ArcSettingIds.UPDATES_CARD,
        stringResource(Res.string.settings_card_updates),
        SettingsIcons.DOWNLOAD,
        entries = listOfNotNull(
            SettingsEntry(ArcSettingIds.VERSION, stringResource(Res.string.settings_version), SettingsControl.Readout(version), keywords = keywords),
            update?.let { SettingsEntry(ArcSettingIds.UPDATE, stringResource(Res.string.settings_update), it, icon = SettingsIcons.DOWNLOAD, keywords = keywords) },
            previous?.let {
                SettingsEntry(
                    ArcSettingIds.SWITCH_BACK,
                    stringResource(Res.string.settings_previous),
                    SettingsControl.Action(stringResource(Res.string.settings_switch_back, it)),
                    icon = SettingsIcons.REFRESH,
                    keywords = keywords,
                )
            },
        ),
    )
}
