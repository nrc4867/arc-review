package dev.chieppa.arc.ui.plugins.settings

import androidx.compose.runtime.Composable
import dev.chieppa.arc.core.settings.SettingsCard
import dev.chieppa.arc.core.settings.SettingsColumn
import dev.chieppa.arc.core.settings.SettingsSection
import dev.chieppa.arc.core.settings.SettingsValue
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.plugins_card
import dev.chieppa.arc.design.generated.plugins_keywords
import dev.chieppa.arc.design.generated.plugins_node
import dev.chieppa.arc.design.generated.plugins_node_download
import dev.chieppa.arc.design.generated.plugins_settings_card
import dev.chieppa.arc.design.generated.plugins_settings_drawn
import dev.chieppa.arc.design.generated.plugins_sources
import dev.chieppa.arc.design.generated.settings_section_plugins
import dev.chieppa.arc.ui.plugins.PluginIds
import dev.chieppa.arc.ui.settings.SettingsIcons
import dev.chieppa.arc.ui.settings.settingsKeywords
import dev.chieppa.arc.ui.settings.SettingsProvider
import dev.chieppa.arc.ui.workbench.Workbench
import org.jetbrains.compose.resources.stringResource

/**
 * The Plugins section of Settings, board 9: the plugin sources, every plugin with its toggle, capabilities and
 * problems, and beside them each running plugin's own page, drawn by the plugin with the components of its panels.
 * Every card is hosted, drawn here rather than from entries.
 */
class PluginsSettingsProvider(private val workbench: Workbench) : SettingsProvider {
    @Composable
    override fun sections(): List<SettingsSection> {
        val plugins = workbench.plugins
        val keywords = settingsKeywords(Res.string.plugins_keywords)
        val node = plugins.manager?.nodeVersion?.let { version ->
            if (plugins.manager.nodeReady()) stringResource(Res.string.plugins_node, version) else stringResource(Res.string.plugins_node_download, version)
        }
        val pages = plugins.running.filter { (_, contributions) -> contributions.settingsPage }.map { (entry, _) ->
            val title = entry.found.manifest?.title ?: entry.id
            SettingsCard(
                PluginIds.settingsCard(entry.id),
                stringResource(Res.string.plugins_settings_card, title),
                SettingsIcons.PLUGIN,
                SettingsColumn.Side,
                hosted = true,
                note = stringResource(Res.string.plugins_settings_drawn, title),
                keywords = listOf(entry.id, title) + keywords,
            )
        }
        val cards = listOf(
            SettingsCard(SOURCES, stringResource(Res.string.plugins_sources), SettingsIcons.FOLDER, hosted = true, keywords = keywords),
            SettingsCard(LIST, stringResource(Res.string.plugins_card), SettingsIcons.PLUGIN, hosted = true, note = node, keywords = keywords),
        ) + pages
        return listOf(SettingsSection(SECTION, stringResource(Res.string.settings_section_plugins), SettingsIcons.PLUGIN, ORDER, cards))
    }

    override fun change(entry: String, value: SettingsValue) = Unit

    @Composable
    override fun Hosted(card: SettingsCard) {
        when (card.id) {
            SOURCES -> PluginSourcesCard(workbench)
            LIST -> PluginListCard(workbench)
            else -> PluginIds.settingsPluginOf(card.id)?.let { PluginSettingsPage(workbench, it) }
        }
    }

    companion object {
        /** The contributor and section id of the Plugins section. */
        const val CONTRIBUTOR = "arc.plugins"
        const val SECTION = "arc.plugins"
        const val SOURCES = "arc.plugins.sources"
        const val LIST = "arc.plugins.list"

        /** After Accounts and before Notifications, as board 9 orders the rail. */
        const val ORDER = 700
    }
}
