package dev.chieppa.arc.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateMapOf
import dev.chieppa.arc.core.settings.SettingsCard
import dev.chieppa.arc.core.settings.SettingsContributor
import dev.chieppa.arc.core.settings.SettingsMerge
import dev.chieppa.arc.core.settings.SettingsOwned
import dev.chieppa.arc.core.settings.SettingsValue

/**
 * The Settings page's contributors, by id: Arc's own under `arc`, and each plugin's under its plugin id. The page asks
 * every one of them and merges their sections, so Arc's sections and a plugin's go through the same path.
 */
class SettingsRegistry {
    private val providers = mutableStateMapOf<String, SettingsProvider>()

    /** Every contributor, in id order. */
    val all: List<Pair<String, SettingsProvider>> get() = providers.entries.sortedBy { it.key }.map { it.key to it.value }

    fun register(id: String, provider: SettingsProvider) {
        require(id !in providers) { "Settings contributor $id is already registered" }
        providers[id] = provider
    }

    /** Takes contributor [id] out, as when its plugin stops; its sections leave the page. */
    fun unregister(id: String) {
        providers.remove(id)
    }

    /** Every contributor's sections, merged into the rail's order. */
    @Composable
    fun sections(): List<SettingsOwned> =
        SettingsMerge.merge(all.map { (id, provider) -> key(id) { SettingsContributor(id, provider.sections()) } })

    /** Hosted [card]'s content, as contributor [contributor] draws it. */
    @Composable
    fun Hosted(contributor: String, card: SettingsCard) {
        providers[contributor]?.Hosted(card)
    }

    /** Sends [value] for entry [entry] to the contributor [contributor]. */
    fun change(contributor: String, entry: String, value: SettingsValue) {
        providers[contributor]?.change(entry, value)
    }
}
