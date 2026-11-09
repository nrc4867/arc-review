package dev.chieppa.arc.ui.runs

import dev.chieppa.arc.core.actions.RunNotify
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.notify_run
import dev.chieppa.arc.design.generated.notify_run_failed
import dev.chieppa.arc.design.generated.notify_run_finished
import dev.chieppa.arc.design.generated.notify_run_stalled
import dev.chieppa.arc.design.generated.notify_run_succeeded
import dev.chieppa.arc.design.generated.notify_run_waits
import dev.chieppa.arc.ui.pulls.PullWatch
import dev.chieppa.arc.ui.services.DesktopNotifier
import org.jetbrains.compose.resources.getString

/** Tells the person of each [RunEvent] through [desktop], in the app's words, counting it on the bell of [pulls]. */
class RunNotifier(private val pulls: PullWatch, private val desktop: DesktopNotifier) {
    /** When the last stall was told, in epoch milliseconds. */
    var lastStallTold: Long? = null
        private set

    suspend fun tell(event: RunEvent, now: Long) {
        val run = event.run
        val title = when (event.kind) {
            RunNotify.MineFailed -> getString(Res.string.notify_run_failed, run.workflowName)
            RunNotify.WaitsOnMe -> getString(Res.string.notify_run_waits, run.workflowName)
            RunNotify.WatchedFinished -> getString(Res.string.notify_run_finished, run.workflowName)
            RunNotify.MineSucceeded -> getString(Res.string.notify_run_succeeded, run.workflowName)
            RunNotify.MineStalled -> getString(Res.string.notify_run_stalled, run.workflowName)
        }
        if (event.kind == RunNotify.MineStalled) lastStallTold = now
        pulls.unseen += 1
        desktop.show(title, getString(Res.string.notify_run, run.repo, run.number, run.title))
    }
}
