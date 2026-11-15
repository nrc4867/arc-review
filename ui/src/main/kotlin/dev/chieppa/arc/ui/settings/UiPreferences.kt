package dev.chieppa.arc.ui.settings

import dev.chieppa.stretch.code.ChangeWash
import dev.chieppa.stretch.theme.StretchTheme

/**
 * The look the person chose: a Stretch theme, an interface size, 1 being normal, whether panes wrap long lines, and
 * whether changed lines are highlighted whole or on their gutter only, leaving the code plain, and whether panes scroll
 * past the end of the file.
 */
data class UiPreferences(
    val theme: StretchTheme = ThemeChoices.DEFAULT,
    val interfaceSize: Float = InterfaceSizes.DEFAULT,
    val wrapLines: Boolean = false,
    val wholeLineHighlight: Boolean = true,
    val scrollPastEnd: Boolean = false,
) {
    /** Where the code and diff viewers wash changed lines. */
    val changeWash: ChangeWash get() = if (wholeLineHighlight) ChangeWash.Line else ChangeWash.Gutter

    /** How far past the last line the code and diff viewers scroll, as a share of their height. */
    val endSpace: Float get() = if (scrollPastEnd) PaneSizes.END_SPACE else 0f
}
