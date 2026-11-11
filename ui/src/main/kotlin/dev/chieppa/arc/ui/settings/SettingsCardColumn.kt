package dev.chieppa.arc.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.chieppa.arc.core.settings.SettingsCard
import dev.chieppa.arc.core.settings.SettingsValue
import dev.chieppa.stretch.theme.StretchSpacing

/** A column of settings [cards], one under another. */
@Composable
fun SettingsCardColumn(cards: List<SettingsCard>, matches: Map<String, List<IntRange>>, onChange: (String, SettingsValue) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(StretchSpacing.space16)) {
        cards.forEach { SettingsCardView(it, matches, onChange) }
    }
}
