package dev.chieppa.arc.ui.settings

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.settings_line_highlight
import dev.chieppa.arc.design.generated.settings_size
import dev.chieppa.arc.design.generated.settings_size_percent
import dev.chieppa.arc.design.generated.settings_theme
import dev.chieppa.arc.design.generated.settings_title
import dev.chieppa.arc.design.generated.settings_wrap
import dev.chieppa.stretch.frame.Dropdown
import dev.chieppa.stretch.frame.Popover
import dev.chieppa.stretch.frame.SettingRow
import dev.chieppa.stretch.ui.Segmented
import dev.chieppa.stretch.ui.Toggle
import org.jetbrains.compose.resources.stringResource

private val WIDTH = 340.dp
private val THEME_FIELD = 200.dp

/**
 * What the top bar's Settings button opens: the theme, the interface size, whether panes wrap long lines unless set
 * otherwise and whether changed lines are highlighted whole or on their gutter only, applied as they change.
 */
@Composable
fun SettingsPopover(open: Boolean, preferences: UiPreferences, onChange: (UiPreferences) -> Unit, onDismiss: () -> Unit) {
    val theme = stringResource(Res.string.settings_theme)
    val sizes = InterfaceSizes.all.associateWith { stringResource(Res.string.settings_size_percent, InterfaceSizes.percent(it)) }
    Popover(open, onDismiss, title = stringResource(Res.string.settings_title), width = WIDTH) {
        SettingRow(theme) {
            Spacer(Modifier.weight(1f))
            Dropdown(
                ThemeChoices.all,
                preferences.theme,
                { onChange(preferences.copy(theme = it)) },
                { it.name },
                theme,
                Modifier.width(THEME_FIELD),
            )
        }
        SettingRow(stringResource(Res.string.settings_size)) {
            Spacer(Modifier.weight(1f))
            Segmented(InterfaceSizes.all, preferences.interfaceSize, { onChange(preferences.copy(interfaceSize = it)) }, { sizes.getValue(it) })
        }
        val wrap = stringResource(Res.string.settings_wrap)
        SettingRow(wrap) {
            Spacer(Modifier.weight(1f))
            Toggle(preferences.wrapLines, { onChange(preferences.copy(wrapLines = it)) }, wrap)
        }
        val lines = stringResource(Res.string.settings_line_highlight)
        SettingRow(lines) {
            Spacer(Modifier.weight(1f))
            Toggle(preferences.wholeLineHighlight, { onChange(preferences.copy(wholeLineHighlight = it)) }, lines)
        }
    }
}
