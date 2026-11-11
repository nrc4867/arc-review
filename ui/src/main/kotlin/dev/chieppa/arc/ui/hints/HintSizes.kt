package dev.chieppa.arc.ui.hints

import androidx.compose.ui.unit.dp

/** The symbol card's measures, as board 6b draws them. */
object HintSizes {
    val width = 560.dp
    val docHeight = 300.dp
    val labelColumn = 88.dp
    val padH = 12.dp
    val padV = 8.dp
    val footerStart = 12.dp
    val footerEnd = 6.dp
    val footerV = 4.dp
    val gap = 6.dp
    val nameGap = 8.dp
    val smallIcon = 14.dp
    val button = 22.dp

    /** How many files' changed lines the cards keep at hand. */
    const val KEPT_CHANGES = 16
}
