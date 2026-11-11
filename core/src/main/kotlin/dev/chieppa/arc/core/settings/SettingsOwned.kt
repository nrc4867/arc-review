package dev.chieppa.arc.core.settings

/** A [section] with the id of the [contributor] that gave it, where changes to its entries go. */
data class SettingsOwned(val contributor: String, val section: SettingsSection)
