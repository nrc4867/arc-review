package dev.chieppa.arc.ui.settings

import dev.chieppa.stretch.theme.StretchTheme

/** The look the person chose: a Stretch theme and an interface size, 1 being normal. */
data class UiPreferences(
    val theme: StretchTheme = ThemeChoices.DEFAULT,
    val interfaceSize: Float = InterfaceSizes.DEFAULT,
)
