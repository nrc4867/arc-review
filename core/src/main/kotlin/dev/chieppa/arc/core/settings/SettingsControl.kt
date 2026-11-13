package dev.chieppa.arc.core.settings

/** How a setting shows its value and takes a new one. Each kind is a subtype. */
sealed interface SettingsControl {
    /** A switch, [on] or off; changing it sends [SettingsValue.Switched]. */
    data class Switch(val on: Boolean) : SettingsControl

    /** One of [options], [selected] by id, drawn in [style]; choosing one sends [SettingsValue.Chosen]. */
    data class Choice(
        val options: List<SettingsOption>,
        val selected: String,
        val style: SettingsChoiceStyle = SettingsChoiceStyle.Menu,
    ) : SettingsControl

    /**
     * A button labelled [label]; pressing it sends [SettingsValue.Pressed]. One not [enabled] shows faint and takes no
     * press; one with [attention] is set apart, as for something that went wrong and can be tried again.
     */
    data class Action(val label: String, val enabled: Boolean = true, val attention: Boolean = false) : SettingsControl

    /** A value to read, not change; with [attention] it is set apart, as for something that needs the person's look. */
    data class Readout(val text: String, val attention: Boolean = false) : SettingsControl
}
