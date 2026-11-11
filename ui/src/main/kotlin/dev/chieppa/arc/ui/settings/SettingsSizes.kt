package dev.chieppa.arc.ui.settings

import androidx.compose.ui.unit.dp

/** Sizes of the Settings page, as the boards draw them, beside Stretch's tokens. */
object SettingsSizes {
    /** The rail of sections, with the search field over it. */
    val rail = 210.dp

    /** A card's header and an entry's least height. */
    val cardHeader = 42.dp
    val entry = 48.dp

    /** How wide a menu choice is. */
    val menuField = 200.dp

    /** The least width that lays a section's side cards beside its main ones, and how much wider the main column is. */
    val twoColumns = 720.dp
    const val MAIN_WEIGHT = 1.25f
    const val SIDE_WEIGHT = 1f

    /** The most a column of cards grows when there is one. */
    val oneColumn = 760.dp
}
