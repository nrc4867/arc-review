package dev.chieppa.arc.core.settings

/**
 * One setting: its [label] with an optional [detail] under it and an [icon] name, and its [control]. Search finds it by
 * its label, detail and [keywords]. Its [id] is unique among its contributor's entries, and a change to it goes back to
 * that contributor by this id.
 */
data class SettingsEntry(
    val id: String,
    val label: String,
    val control: SettingsControl,
    val detail: String? = null,
    val icon: String? = null,
    val keywords: List<String> = emptyList(),
)
