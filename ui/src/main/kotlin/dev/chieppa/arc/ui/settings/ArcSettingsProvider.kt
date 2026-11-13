package dev.chieppa.arc.ui.settings

import androidx.compose.runtime.Composable
import dev.chieppa.arc.core.settings.SettingsSection
import dev.chieppa.arc.core.settings.SettingsValue
import dev.chieppa.arc.ui.update.updatesCard
import dev.chieppa.arc.ui.workbench.Workbench

/**
 * Arc's own settings sections, given through the settings registry like any contributor's: General, Appearance,
 * Accounts and Notifications. A change goes into the workbench's preferences or watchlists, which save it at once;
 * the Updates card's buttons go to the workbench's updates.
 */
class ArcSettingsProvider(private val workbench: Workbench) : SettingsProvider {
    @Composable
    override fun sections(): List<SettingsSection> = listOf(
        generalSection(workbench.preferences, updatesCard(workbench.updates)),
        appearanceSection(workbench.preferences),
        accountsSection(workbench.watch),
        notificationsSection(workbench.watch.watchlist, workbench.runs.watch.watchlist),
    )

    override fun change(entry: String, value: SettingsValue) {
        val preferences = workbench.preferences
        when {
            value is SettingsValue.Switched && entry == ArcSettingIds.WRAP -> workbench.preferences = preferences.copy(wrapLines = value.on)
            value is SettingsValue.Switched && entry == ArcSettingIds.LINE_HIGHLIGHT -> workbench.preferences = preferences.copy(wholeLineHighlight = value.on)
            value is SettingsValue.Chosen && entry == ArcSettingIds.THEME -> workbench.preferences = preferences.copy(theme = ThemeChoices.byId(value.option))
            value is SettingsValue.Chosen && entry == ArcSettingIds.SIZE ->
                InterfaceSizes.byId(value.option)?.let { workbench.preferences = preferences.copy(interfaceSize = it) }
            value is SettingsValue.Pressed && entry == ArcSettingIds.UPDATE -> workbench.updates.press()
            value is SettingsValue.Pressed && entry == ArcSettingIds.SWITCH_BACK -> workbench.updates.switchBack()
            value is SettingsValue.Switched -> switchNotification(entry, value.on)
        }
    }

    private fun switchNotification(entry: String, on: Boolean) {
        ArcSettingIds.pullRuleOf(entry)?.let { id ->
            val watch = workbench.watch
            watch.watchlist.rules.firstOrNull { it.id == id }?.let { watch.putRule(it.switched(on)) }
        }
        ArcSettingIds.runNotifyOf(entry)?.let { kind ->
            val runs = workbench.runs.watch
            runs.watchlist = runs.watchlist.withNotify(kind, on)
        }
    }
}
