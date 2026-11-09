package dev.chieppa.arc.ui.runs

import dev.chieppa.arc.ui.actions.ArcActionIds

/** The run actions a run's context menu offers, in order. */
object RunActionList {
    val ROW = listOf(
        ArcActionIds.OPEN_RUN,
        ArcActionIds.OPEN_RUN_COMMIT,
        ArcActionIds.WATCH_RUN,
        ArcActionIds.WATCH_WORKFLOW,
        ArcActionIds.RERUN_RUN,
        ArcActionIds.RERUN_FAILED,
        ArcActionIds.CANCEL_RUN,
        ArcActionIds.APPROVE_FORK_RUN,
        ArcActionIds.RUN_WORKFLOW_AGAIN,
        ArcActionIds.OPEN_RUN_ON_GITHUB,
        ArcActionIds.COPY_RUN_LINK,
    )
}
