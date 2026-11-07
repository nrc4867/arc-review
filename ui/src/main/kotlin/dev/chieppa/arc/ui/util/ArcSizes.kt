package dev.chieppa.arc.ui.util

import androidx.compose.ui.unit.dp

/** Sizes of Arc's own pieces, as the boards draw them, beside Stretch's tokens. */
object ArcSizes {
    /** The source strip under the top bar. */
    val sourceStrip = 36.dp
    val sourceChipHeight = 28.dp
    val sourceTitleMax = 180.dp
    val chipClose = 20.dp
    val ring = 12.dp
    val ringStroke = 2.dp

    /** A pane's tool row under its tabs. */
    val toolRow = 34.dp

    /** How wide a tool row shows the mark key beside its button, and its revisions. */
    val toolRowWide = 900.dp
    val toolRowRoomy = 720.dp
    val revisionChipHeight = 22.dp

    /** The mark box at the end of a tree row. */
    val markBox = 14.dp
    val markBoxIcon = 10.dp
    val statusLetter = 14.dp

    /** The floating find bar. */
    val findWidth = 500.dp
    val findField = 200.dp

    val threadMaxWidth = 720.dp
    val avatar = 18.dp
    val gutterDot = 7.dp
    val binaryIcon = 32.dp

    /** The bar over each side of a changed picture, in the removed or added color. */
    val imageSideBar = 2.dp
    val stagedChip = 92.dp
    val stagedSource = 150.dp
    val stagedLocation = 300.dp
    val finderWidth = 560.dp
    val finderTop = 96
    val finderRows = 14
    val popoverWidth = 380.dp
    val reviewSelector = 200.dp
    val goToAnything = 260.dp

    /** The Pull requests drawer, and a saved filter's tab: its tool row, list rows and columns. */
    const val pullRequestsDrawer = 320f
    val filterToolRow = 48.dp
    val filterField = 30.dp
    val groupDropdown = 170.dp
    val pullHeader = 30.dp
    val pullRow = 48.dp
    val pullGroup = 30.dp
    val newDot = 6.dp
    val pullIcon = 16.dp
    val labelsColumn = 170.dp
    val waitingColumn = 160.dp
    val checkColumn = 22.dp
    val commentsColumn = 40.dp
    val updatedColumn = 70.dp
    val actionColumn = 120.dp
    val labelPill = 18.dp
    val labelDot = 7.dp
    val waitingPill = 20.dp
    val smallIcon = 12.dp
    val checkMark = 14.dp
    val smallAvatar = 16.dp
    val separatorDot = 3.dp
    val addButton = 24.dp
    val ruleRow = 40.dp
    val newFilterButton = 28.dp
    val notifyHeader = 28.dp
    val drawerFooter = 6.dp
}
