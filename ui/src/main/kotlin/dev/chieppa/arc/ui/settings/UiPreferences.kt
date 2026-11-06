package dev.chieppa.arc.ui.settings

import dev.chieppa.stretch.theme.StretchTheme

/** The look the person chose: a Stretch theme, an interface size, 1 being normal, and whether panes wrap long lines. */
data class UiPreferences(
    val theme: StretchTheme = ThemeChoices.DEFAULT,
    val interfaceSize: Float = InterfaceSizes.DEFAULT,
    val wrapLines: Boolean = false,
)
