package dev.chieppa.arc.core.settings

/**
 * A card in a settings section: a [title] under an [icon] name, its [entries] one per row, in [column]. A [hosted] card
 * is drawn by its contributor, as a plugin draws its own settings page, in place of entries; [note] is quiet text in
 * its header, and search finds it by its title and [keywords].
 */
data class SettingsCard(
    val id: String,
    val title: String,
    val icon: String? = null,
    val column: SettingsColumn = SettingsColumn.Main,
    val entries: List<SettingsEntry> = emptyList(),
    val hosted: Boolean = false,
    val note: String? = null,
    val keywords: List<String> = emptyList(),
)
