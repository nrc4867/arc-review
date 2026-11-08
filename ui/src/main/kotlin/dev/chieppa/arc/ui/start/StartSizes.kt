package dev.chieppa.arc.ui.start

import androidx.compose.ui.unit.dp

/** The start screen's measures, as the boards draw them. */
object StartSizes {
    val maxWidth = 1120.dp
    val paddingTop = 32.dp
    val paddingSide = 40.dp
    val paddingBottom = 24.dp
    val sectionGap = 22.dp
    val cardGap = 16.dp

    /** How much wider the main column is than the side one. */
    const val MAIN_WEIGHT = 1.55f
    const val SIDE_WEIGHT = 1f

    val cardHeader = 38.dp
    val row = 44.dp
    val rowSmall = 34.dp
    val keyRow = 32.dp
    val settingRow = 40.dp
    val settingField = 160.dp
    val openField = 40.dp
    val goButton = 36.dp
    val filterChip = 28.dp
    val filterIcon = 13.dp
    val headerIcon = 20.dp
    const val TITLE_SP = 22f
    const val TITLE_LINE_SP = 28f
    val bar = 8.dp
    val barGap = 2.dp
    val barHatch = 3.dp
    val tableBar = 100.dp
    val tableCount = 44.dp
    val tableAction = 140.dp
    val tableRow = 52.dp
    val legendSwatch = 8.dp
}
