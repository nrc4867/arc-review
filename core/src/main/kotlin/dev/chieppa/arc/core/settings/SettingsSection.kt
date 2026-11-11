package dev.chieppa.arc.core.settings

/**
 * One section of the Settings page, a row in its rail. Its [id] is namespaced by whoever contributes it
 * (`arc.general`, `speckit.settings`). It shows [label] under the [icon] name, sits by [order] among the others
 * (lower first) and holds [cards].
 */
data class SettingsSection(
    val id: String,
    val label: String,
    val icon: String? = null,
    val order: Int = 0,
    val cards: List<SettingsCard> = emptyList(),
)
