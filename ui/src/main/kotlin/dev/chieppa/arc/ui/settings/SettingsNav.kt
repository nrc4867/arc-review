package dev.chieppa.arc.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.chieppa.arc.core.settings.SettingsOwned
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.settings_search
import dev.chieppa.stretch.frame.ListRow
import dev.chieppa.stretch.frame.RowIcon
import dev.chieppa.stretch.frame.RowText
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchIcons
import dev.chieppa.stretch.theme.StretchSpacing
import dev.chieppa.stretch.ui.TextField
import dev.chieppa.stretch.ui.VerticalDivider
import org.jetbrains.compose.resources.stringResource

/**
 * The Settings page's rail: the search field over a row for each of [sections], the [selected] one highlighted. A click
 * on a row reports its section's id to [onSelect]; typing reports the query to [onQuery].
 */
@Composable
fun SettingsNav(sections: List<SettingsOwned>, selected: String?, query: String, onQuery: (String) -> Unit, onSelect: (String) -> Unit) {
    val c = Stretch.colors
    Row(Modifier.fillMaxHeight()) {
        Column(
            Modifier.width(SettingsSizes.rail).fillMaxHeight().background(c.surfaceRail).padding(horizontal = StretchSpacing.space8, vertical = StretchSpacing.space10),
            verticalArrangement = Arrangement.spacedBy(StretchSpacing.space2),
        ) {
            TextField(query, onQuery, Modifier.fillMaxWidth(), placeholder = stringResource(Res.string.settings_search), icon = StretchIcons.Search)
            Spacer(Modifier.height(StretchSpacing.space6))
            for (owned in sections) {
                val section = owned.section
                ListRow(section.id == selected, { onSelect(section.id) }) {
                    SettingsIcons.of(section.icon)?.let { RowIcon(it) }
                    RowText(section.label)
                }
            }
        }
        VerticalDivider()
    }
}
