package dev.chieppa.arc.core.settings

/**
 * What a search found in one section: the [owned] section, only its [cards] that hold matches, each keeping only its
 * matching entries, and the parts of each matching entry's label to mark, by entry id, in [matches].
 */
data class SettingsFound(
    val owned: SettingsOwned,
    val cards: List<SettingsCard>,
    val matches: Map<String, List<IntRange>>,
)
