package dev.chieppa.arc.core.settings

/** Puts every contributor's sections together into the Settings page's rail. */
object SettingsMerge {
    /**
     * The sections of [contributors] with their owners, by order and then label. A section with no entries and no hosted
     * card is left out, and of two with one id the first contributor's stays.
     */
    fun merge(contributors: List<SettingsContributor>): List<SettingsOwned> =
        contributors
            .flatMap { contributor -> contributor.sections.map { SettingsOwned(contributor.id, it) } }
            .filter { owned -> owned.section.cards.any { it.hosted || it.entries.isNotEmpty() } }
            .distinctBy { it.section.id }
            .sortedWith(compareBy({ it.section.order }, { it.section.label.lowercase() }))
}
