package dev.chieppa.arc.ui.settings

import dev.chieppa.stretch.code.ChangeWash
import dev.chieppa.stretch.theme.StretchTheme

/**
 * The look the person chose: a Stretch theme, an interface size, 1 being normal, whether panes wrap long lines, and
 * whether changed lines are highlighted on their gutter only, leaving the code plain.
 */
data class UiPreferences(
    val theme: StretchTheme = ThemeChoices.DEFAULT,
    val interfaceSize: Float = InterfaceSizes.DEFAULT,
    val wrapLines: Boolean = false,
    val gutterHighlight: Boolean = false,
) {
    /** Where the code and diff viewers wash changed lines. */
    val changeWash: ChangeWash get() = if (gutterHighlight) ChangeWash.Gutter else ChangeWash.Line
}
