package dev.chieppa.arc.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import dev.chieppa.arc.core.settings.SettingsCard
import dev.chieppa.arc.core.settings.SettingsFound
import dev.chieppa.arc.core.settings.SettingsValue
import dev.chieppa.stretch.theme.Icon
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchSpacing
import dev.chieppa.stretch.ui.Label

/**
 * What a search found, section by section: each section's icon and label over its cards that hold matches, each card
 * keeping only its matching entries with the matches marked. A change goes to [onChange] with the section's
 * contributor and the entry's id; [hosted] draws a hosted card's content for its contributor. Nothing found shows
 * nothing.
 */
@Composable
fun SettingsSearchResults(
    found: List<SettingsFound>,
    onChange: (String, String, SettingsValue) -> Unit,
    hosted: @Composable (String, SettingsCard) -> Unit = { _, _ -> },
) {
    val c = Stretch.colors
    Column(verticalArrangement = Arrangement.spacedBy(StretchSpacing.space24)) {
        for (result in found) {
            val section = result.owned.section
            Column(verticalArrangement = Arrangement.spacedBy(StretchSpacing.space10)) {
                Row(horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space8), verticalAlignment = Alignment.CenterVertically) {
                    SettingsIcons.of(section.icon)?.let { Icon(it, c.inkMuted) }
                    Label(section.label.uppercase(), Stretch.type.eyebrow, c.inkMuted)
                }
                SettingsCardsView(result.cards, result.matches, { entry, value -> onChange(result.owned.contributor, entry, value) }) {
                    hosted(result.owned.contributor, it)
                }
            }
        }
    }
}
