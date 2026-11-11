package dev.chieppa.arc.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.chieppa.arc.core.settings.SettingsCard
import dev.chieppa.arc.core.settings.SettingsColumn
import dev.chieppa.arc.core.settings.SettingsValue
import dev.chieppa.stretch.theme.StretchSpacing

/**
 * A section's [cards]: its side cards in a narrower column beside the main ones where the width allows, else every
 * card in one column, main cards first. [matches] marks the parts of entry labels a search found; [hosted] draws a
 * hosted card's content.
 */
@Composable
fun SettingsCardsView(
    cards: List<SettingsCard>,
    matches: Map<String, List<IntRange>>,
    onChange: (String, SettingsValue) -> Unit,
    hosted: @Composable (SettingsCard) -> Unit = {},
) {
    val main = cards.filter { it.column == SettingsColumn.Main }
    val side = cards.filter { it.column == SettingsColumn.Side }
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        if (side.isNotEmpty() && main.isNotEmpty() && maxWidth >= SettingsSizes.twoColumns) {
            Row(horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space16), verticalAlignment = Alignment.Top) {
                SettingsCardColumn(main, matches, onChange, hosted, Modifier.weight(SettingsSizes.MAIN_WEIGHT))
                SettingsCardColumn(side, matches, onChange, hosted, Modifier.weight(SettingsSizes.SIDE_WEIGHT))
            }
        } else {
            SettingsCardColumn(main + side, matches, onChange, hosted, Modifier.widthIn(max = SettingsSizes.oneColumn))
        }
    }
}
