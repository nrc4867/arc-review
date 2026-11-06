package dev.chieppa.arc.ui.settings

import dev.chieppa.stretch.theme.StretchTheme
import dev.chieppa.stretch.theme.StretchThemes

/** The themes Arc offers: every one Stretch ships. */
object ThemeChoices {
    val DEFAULT: StretchTheme = StretchThemes.WorkshopDark

    val all: List<StretchTheme> get() = StretchThemes.all

    /** The theme with [id], or the default when there is none. */
    fun byId(id: String?): StretchTheme = all.firstOrNull { it.id == id } ?: DEFAULT
}
