package dev.chieppa.arc.core.settings

/** A new value the person gave a setting, sent to the contributor of its entry. */
sealed interface SettingsValue {
    /** A switch turned [on] or off. */
    data class Switched(val on: Boolean) : SettingsValue

    /** The option with id [option] chosen. */
    data class Chosen(val option: String) : SettingsValue

    /** A button pressed. */
    data object Pressed : SettingsValue
}
