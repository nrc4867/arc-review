package dev.chieppa.arc.core.settings

/** A card in a settings section: a [title] under an [icon] name, its [entries] one per row, in [column]. */
data class SettingsCard(
    val id: String,
    val title: String,
    val icon: String? = null,
    val column: SettingsColumn = SettingsColumn.Main,
    val entries: List<SettingsEntry> = emptyList(),
)
