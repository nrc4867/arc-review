package dev.chieppa.arc.ui.settings

import dev.chieppa.arc.core.pane.Page
import dev.chieppa.arc.core.settings.SettingsOwned

/** Which section a Settings page shows. */
object SettingsShown {
    /** The section of [sections] [page] names, or the first when it names none of them. */
    fun of(sections: List<SettingsOwned>, page: Page.Settings): SettingsOwned? =
        sections.firstOrNull { it.section.id == page.section } ?: sections.firstOrNull()
}
