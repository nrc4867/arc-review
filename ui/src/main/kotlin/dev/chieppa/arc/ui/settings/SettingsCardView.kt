package dev.chieppa.arc.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import dev.chieppa.arc.core.settings.SettingsCard
import dev.chieppa.arc.core.settings.SettingsValue
import dev.chieppa.stretch.theme.Icon
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchSpacing
import dev.chieppa.stretch.ui.Card
import dev.chieppa.stretch.ui.Divider
import dev.chieppa.stretch.ui.Label

/**
 * A settings card: its icon, title and note over its entries, each with the parts of its label a search marked in
 * [matches], or over what [hosted] draws for a hosted card. A change to an entry goes to [onChange] with the entry's
 * id. The card is tagged with its id.
 */
@Composable
fun SettingsCardView(
    card: SettingsCard,
    matches: Map<String, List<IntRange>>,
    onChange: (String, SettingsValue) -> Unit,
    modifier: Modifier = Modifier,
    hosted: @Composable (SettingsCard) -> Unit = {},
) {
    val c = Stretch.colors
    Card(modifier.fillMaxWidth().testTag(card.id)) {
        Row(
            Modifier.fillMaxWidth().heightIn(min = SettingsSizes.cardHeader).padding(start = StretchSpacing.space14, end = StretchSpacing.space8),
            horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space8),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SettingsIcons.of(card.icon)?.let { Icon(it, c.inkControl) }
            Label(card.title, Stretch.type.drawerTitle, c.inkStrong, Modifier.weight(1f))
            card.note?.let { Label(it, Stretch.type.bodySm, c.inkSubtle) }
        }
        if (card.hosted) {
            Divider()
            hosted(card)
        }
        for (entry in card.entries) {
            Divider()
            SettingsEntryRow(entry, matches[entry.id].orEmpty()) { onChange(entry.id, it) }
        }
    }
}
