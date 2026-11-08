package dev.chieppa.arc.ui.start

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import dev.chieppa.arc.design.ArcIcons
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.settings_line_highlight
import dev.chieppa.arc.design.generated.settings_size
import dev.chieppa.arc.design.generated.settings_size_percent
import dev.chieppa.arc.design.generated.settings_theme
import dev.chieppa.arc.design.generated.settings_wrap
import dev.chieppa.arc.ui.settings.InterfaceSizes
import dev.chieppa.arc.ui.settings.ThemeChoices
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.frame.Dropdown
import dev.chieppa.stretch.theme.Icon
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchIcons
import dev.chieppa.stretch.theme.StretchSpacing
import dev.chieppa.stretch.ui.ControlLabel
import dev.chieppa.stretch.ui.Divider
import dev.chieppa.stretch.ui.Segmented
import dev.chieppa.stretch.ui.Toggle
import org.jetbrains.compose.resources.stringResource

/**
 * The Settings card's rows: the theme, the interface size, whether panes wrap long lines and whether changed lines are
 * highlighted whole, each applied as it changes, as the Settings popover does.
 */
@Composable
fun SettingsRows(workbench: Workbench) {
    val c = Stretch.colors
    val preferences = workbench.preferences
    val theme = stringResource(Res.string.settings_theme)
    val wrap = stringResource(Res.string.settings_wrap)
    val lines = stringResource(Res.string.settings_line_highlight)
    val sizes = InterfaceSizes.all.associateWith { stringResource(Res.string.settings_size_percent, InterfaceSizes.percent(it)) }
    val rows = listOf<Triple<ImageVector, String, @Composable () -> Unit>>(
        Triple(ArcIcons.Palette, theme) {
            Dropdown(ThemeChoices.all, preferences.theme, { workbench.preferences = preferences.copy(theme = it) }, { it.name }, theme, Modifier.width(StartSizes.settingField))
        },
        Triple(StretchIcons.Fit, stringResource(Res.string.settings_size)) {
            Segmented(InterfaceSizes.all, preferences.interfaceSize, { workbench.preferences = preferences.copy(interfaceSize = it) }, { sizes.getValue(it) }, compact = true)
        },
        Triple(ArcIcons.Wrap, wrap) { Toggle(preferences.wrapLines, { workbench.preferences = preferences.copy(wrapLines = it) }, wrap) },
        Triple(ArcIcons.ChangedSince, lines) { Toggle(preferences.wholeLineHighlight, { workbench.preferences = preferences.copy(wholeLineHighlight = it) }, lines) },
    )
    rows.forEachIndexed { i, (icon, label, control) ->
        if (i > 0) Divider()
        Row(
            Modifier.fillMaxWidth().heightIn(min = StartSizes.settingRow).padding(horizontal = StretchSpacing.space14),
            horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space10),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, c.inkControl)
            ControlLabel(label, Stretch.type.body, c.ink, Modifier.weight(1f))
            control()
        }
    }
}
