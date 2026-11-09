package dev.chieppa.arc.ui.runs

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.chieppa.stretch.theme.StretchSpacing

/** Which of a run list's optional columns are shown; the state, run and repo columns always are. */
data class RunColumns(val time: Boolean, val jobs: Boolean, val why: Boolean, val actions: Boolean) {
    companion object {
        val All = RunColumns(time = true, jobs = true, why = true, actions = true)

        /**
         * The columns that fit a list [width] wide while the run's own column keeps [RunSizes.titleMin]: time first,
         * then jobs, why it is the person's, and the row's buttons.
         */
        fun fitting(width: Dp): RunColumns {
            val gap = StretchSpacing.space12
            var left = width - gap * 2 - RunSizes.stateColumn - gap - RunSizes.titleMin - gap - RunSizes.sourceColumn
            fun take(column: Dp): Boolean = (left >= gap + column).also { if (it) left -= gap + column }
            val time = take(RunSizes.timeColumn)
            val jobs = time && take(RunSizes.jobsColumn)
            val why = jobs && take(RunSizes.whyColumn)
            val actions = why && take(RunSizes.actionsColumn)
            return RunColumns(time, jobs, why, actions)
        }

        /** The width a side column [wanted] wide takes beside a page [available] wide. */
        fun side(wanted: Dp, available: Dp): Dp = minOf(wanted, available * RunSizes.SIDE_SHARE).coerceAtLeast(0.dp)
    }
}
