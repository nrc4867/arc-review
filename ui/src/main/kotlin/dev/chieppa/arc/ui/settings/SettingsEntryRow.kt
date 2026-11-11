package dev.chieppa.arc.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import dev.chieppa.arc.core.settings.SettingsEntry
import dev.chieppa.arc.core.settings.SettingsValue
import dev.chieppa.stretch.frame.MatchedText
import dev.chieppa.stretch.theme.Icon
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchSpacing
import dev.chieppa.stretch.ui.Label

/**
 * One setting in a card: its icon, its label with the parts a search [matched] marked and its detail under it, then its
 * control. The row is tagged with the entry's id.
 */
@Composable
fun SettingsEntryRow(entry: SettingsEntry, matched: List<IntRange>, onChange: (SettingsValue) -> Unit) {
    val c = Stretch.colors
    Row(
        Modifier.fillMaxWidth().heightIn(min = SettingsSizes.entry).padding(horizontal = StretchSpacing.space14, vertical = StretchSpacing.space6).testTag(entry.id),
        horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space12),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SettingsIcons.of(entry.icon)?.let { Icon(it, c.inkControl) }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(StretchSpacing.space2)) {
            MatchedText(entry.label, matched, Stretch.type.body.copy(fontWeight = FontWeight.Medium), c.inkStrong)
            entry.detail?.let { Label(it, Stretch.type.bodySm, c.inkSubtle) }
        }
        SettingsControlView(entry.control, entry.label, onChange)
    }
}
