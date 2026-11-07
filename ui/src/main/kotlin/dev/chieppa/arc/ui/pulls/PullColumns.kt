package dev.chieppa.arc.ui.pulls

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.chieppa.arc.ui.util.ArcSizes
import dev.chieppa.stretch.theme.StretchSpacing

/**
 * Which of a saved filter's columns fit beside the title, which keeps at least [ArcSizes.pullTitleMin] and takes all
 * the room left over. As the list narrows, labels go first, then comments, then its age, then who it waits on.
 */
data class PullColumns(val labels: Boolean, val waiting: Boolean, val comments: Boolean, val updated: Boolean) {
    companion object {
        val ALL = PullColumns(labels = true, waiting = true, comments = true, updated = true)

        /** The columns that fit a list [width] wide. */
        fun fit(width: Dp): PullColumns {
            val gap = StretchSpacing.space12
            // The dot, icon, check and action columns always show, with the title: four gaps between those five.
            val fixed = gap * 2 + ArcSizes.newDot + ArcSizes.pullIcon + ArcSizes.checkColumn + ArcSizes.actionColumn + gap * 4
            var room = width - fixed - ArcSizes.pullTitleMin
            fun take(column: Dp): Boolean {
                if (room < column + gap) {
                    room = 0.dp
                    return false
                }
                room -= column + gap
                return true
            }
            val waiting = take(ArcSizes.waitingColumn)
            val updated = take(ArcSizes.updatedColumn)
            val comments = take(ArcSizes.commentsColumn)
            val labels = take(ArcSizes.labelsColumn)
            return PullColumns(labels, waiting, comments, updated)
        }
    }
}
