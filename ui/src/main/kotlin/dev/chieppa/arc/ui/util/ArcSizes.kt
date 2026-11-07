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

    /** The switcher: how wide it is, how far down the window, the widest a row's ref grows, its rows' progress ring. */
    val switcherWidth = 640.dp
    val switcherTop = 52.dp
    val switcherRef = 220.dp
    val switcherRing = 14.dp
    val linkCardIndent = 24.dp
    val popoverWidth = 380.dp
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
    val pullTitleMin = 320.dp
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
    val ruleRow = 40.dp
    val newFilterButton = 28.dp
    val notifyHeader = 28.dp
    val drawerFooter = 6.dp

    /** The Commits drawer and the revision matrix: its tool row, columns, rows, cells and their marks. */
    const val commitsDrawer = 280f
    val commitRow = 44.dp
    val matrixToolRow = 36.dp
    val matrixToggle = 24.dp
    val matrixFileColumn = 260.dp
    val matrixColumn = 104.dp
    val matrixRow = 34.dp
    val matrixCell = 24.dp
    val matrixCellIcon = 12.dp
    val matrixRing = 2.dp
    val threadDot = 7.dp
    val threadDotEdge = 1.5.dp
    val threadCountGap = 3.dp
    val unchangedDot = 5.dp
    val legendSwatchWidth = 22.dp
    val legendSwatchHeight = 16.dp
}
