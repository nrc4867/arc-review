package dev.chieppa.arc.ui.settings

import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.chieppa.arc.core.settings.SettingsChoiceStyle
import dev.chieppa.arc.core.settings.SettingsControl
import dev.chieppa.arc.core.settings.SettingsValue
import dev.chieppa.stretch.frame.Dropdown
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchIcons
import dev.chieppa.stretch.ui.Button
import dev.chieppa.stretch.ui.Label
import dev.chieppa.stretch.ui.Segmented
import dev.chieppa.stretch.ui.Toggle

/** An entry's control, drawn with Stretch's: a switch, a menu or segments for a choice, a button, or text to read. */
@Composable
fun SettingsControlView(control: SettingsControl, label: String, onChange: (SettingsValue) -> Unit) {
    val c = Stretch.colors
    when (control) {
        is SettingsControl.Switch -> Toggle(control.on, { onChange(SettingsValue.Switched(it)) }, label)
        is SettingsControl.Choice -> {
            val selected = control.options.firstOrNull { it.id == control.selected } ?: control.options.firstOrNull() ?: return
            when (control.style) {
                SettingsChoiceStyle.Menu ->
                    Dropdown(control.options, selected, { onChange(SettingsValue.Chosen(it.id)) }, { it.label }, label, Modifier.width(SettingsSizes.menuField))
                SettingsChoiceStyle.Segments ->
                    Segmented(control.options, selected, { onChange(SettingsValue.Chosen(it.id)) }, { it.label }, compact = true)
            }
        }
        is SettingsControl.Action -> Button(
            control.label,
            { onChange(SettingsValue.Pressed) },
            icon = if (control.attention) StretchIcons.Warn else null,
            enabled = control.enabled,
        )
        is SettingsControl.Readout -> Label(control.text, Stretch.type.body, if (control.attention) c.warnInk else c.ink)
    }
}
