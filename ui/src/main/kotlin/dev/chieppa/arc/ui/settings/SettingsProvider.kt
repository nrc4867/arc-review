package dev.chieppa.arc.ui.settings

import androidx.compose.runtime.Composable
import dev.chieppa.arc.core.settings.SettingsCard
import dev.chieppa.arc.core.settings.SettingsSection
import dev.chieppa.arc.core.settings.SettingsValue

/**
 * What one contributor gives the Settings page: its sections as plain data, asked again whenever what they read
 * changes, and where a new value the person gives one of its entries goes.
 */
interface SettingsProvider {
    @Composable
    fun sections(): List<SettingsSection>

    /** The person gave entry [entry] the value [value]; the contributor saves and applies it. */
    fun change(entry: String, value: SettingsValue)

    /** Draws hosted [card]'s content under its header. */
    @Composable
    fun Hosted(card: SettingsCard) {}
}
