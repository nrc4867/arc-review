package dev.chieppa.arc.ui.navigation

import androidx.compose.ui.unit.dp

/** The navigation list's and the outline's measures, as the code navigation board draws them. */
object NavigationSizes {
    val popupWidth = 600.dp
    val pathColumn = 190.dp
    val row = 28.dp
    val outlineIndent = 14.dp
    val outlineRow = 24.dp
    val smallIcon = 12.dp

    /** How opaque a filtered candidate or usage draws, below the confident ones. */
    const val FILTERED_ALPHA = 0.5f

    /** Lines a revealed name keeps showing above it. */
    const val LEAD_IN = 3
}
