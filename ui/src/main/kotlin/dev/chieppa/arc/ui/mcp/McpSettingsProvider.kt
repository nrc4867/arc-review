package dev.chieppa.arc.ui.mcp

import androidx.compose.runtime.Composable
import dev.chieppa.arc.core.settings.SettingsCard
import dev.chieppa.arc.core.settings.SettingsSection
import dev.chieppa.arc.core.settings.SettingsValue
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.mcp_card
import dev.chieppa.arc.design.generated.mcp_keywords
import dev.chieppa.arc.design.generated.settings_section_mcp
import dev.chieppa.arc.ui.settings.SettingsIcons
import dev.chieppa.arc.ui.settings.SettingsProvider
import dev.chieppa.arc.ui.settings.settingsKeywords
import dev.chieppa.arc.ui.workbench.Workbench
import org.jetbrains.compose.resources.stringResource

/**
 * The MCP server section of Settings, a section of its own because the server serves Arc as a whole, not plugins only:
 * its switch, where it listens and how many clients are connected, and the command that connects Claude Code. Where
 * the app serves no MCP there is no section.
 */
class McpSettingsProvider(private val workbench: Workbench) : SettingsProvider {
    @Composable
    override fun sections(): List<SettingsSection> {
        if (!workbench.mcp.available) return emptyList()
        val keywords = settingsKeywords(Res.string.mcp_keywords)
        val card = SettingsCard(CARD, stringResource(Res.string.mcp_card), SettingsIcons.SERVER, hosted = true, keywords = keywords)
        return listOf(SettingsSection(SECTION, stringResource(Res.string.settings_section_mcp), SettingsIcons.SERVER, ORDER, listOf(card)))
    }

    override fun change(entry: String, value: SettingsValue) = Unit

    @Composable
    override fun Hosted(card: SettingsCard) {
        if (card.id == CARD) McpServerCard(workbench)
    }

    companion object {
        const val CONTRIBUTOR = "arc.mcp"
        const val SECTION = "arc.mcp"
        const val CARD = "arc.mcp.server"

        /** After Plugins and before Notifications. */
        const val ORDER = 750
    }
}
