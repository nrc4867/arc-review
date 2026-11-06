package dev.chieppa.arc.ui.util

import androidx.compose.ui.unit.dp

/** Sizes of Arc's own pieces, as the boards draw them, beside Stretch's tokens. */
object ArcSizes {
    /** The source strip under the top bar. */
    val sourceStrip = 36.dp
    val sourceChipHeight = 28.dp
    val sourceTitleMax = 180.dp
    val ring = 12.dp
    val ringStroke = 2.dp

    /** A pane's tool row under its tabs. */
    val toolRow = 34.dp
    val revisionChipHeight = 22.dp

    /** The mark box at the end of a tree row. */
    val markBox = 14.dp
    val markBoxIcon = 10.dp
    val statusLetter = 14.dp

    /** The floating find bar. */
    val findWidth = 440.dp
    val findField = 200.dp

    val threadMaxWidth = 720.dp
    val avatar = 18.dp
    val gutterDot = 7.dp
    val gutterBar = 3.dp
    val stagedChip = 92.dp
    val stagedSource = 72.dp
    val stagedLocation = 300.dp
    val finderWidth = 560.dp
    val finderTop = 96
    val finderRows = 14
    val popoverWidth = 380.dp
    val reviewSelector = 200.dp
    val goToAnything = 260.dp
}
