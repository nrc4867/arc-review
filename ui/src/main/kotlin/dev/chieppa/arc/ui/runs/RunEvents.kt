package dev.chieppa.arc.ui.runs

import dev.chieppa.arc.core.actions.MineFacts
import dev.chieppa.arc.core.actions.MineRule
import dev.chieppa.arc.core.actions.MineRules
import dev.chieppa.arc.core.actions.RunNotify
import dev.chieppa.arc.core.actions.RunRef
import dev.chieppa.arc.core.actions.RunState
import dev.chieppa.arc.core.actions.WhyMine
import dev.chieppa.arc.core.actions.WorkflowRun
import dev.chieppa.arc.core.actions.timing.RunStall

/** Which notifications that are on a poll sets off, at most one per run. */
object RunEvents {
    private val ALL_RULES = MineRule.entries.toSet()

    /**
     * What changed between a repo's runs [before] and [after]: a run of the person's that failed or succeeded, or a
     * watched run that finished. A run not in [before] counts when it was updated after [since]. With nothing
     * [before], the repo is being polled for the first time, so nothing is set off.
     */
    fun finished(before: List<WorkflowRun>?, after: List<WorkflowRun>, facts: MineFacts, on: Set<RunNotify>, since: Long): List<RunEvent> {
        if (before == null) return emptyList()
        val previous = before.associate { it.id to it.state }
        return after.mapNotNull { run ->
            val was = previous[run.id] ?: if (run.updatedAt.epochSecond * 1000 > since) RunState.Running else return@mapNotNull null
            if (was.finished || !run.state.finished) return@mapNotNull null
            val reasons = MineRules.reasons(run, facts, ALL_RULES)
            val mine = reasons.any { it == WhyMine.StartedByMe || it == WhyMine.MyPull || it == WhyMine.AssignedPull }
            when {
                run.state == RunState.Failed && mine && RunNotify.MineFailed in on -> RunEvent(RunNotify.MineFailed, run)
                WhyMine.Watched in reasons && RunNotify.WatchedFinished in on -> RunEvent(RunNotify.WatchedFinished, run)
                run.state == RunState.Succeeded && WhyMine.StartedByMe in reasons && RunNotify.MineSucceeded in on -> RunEvent(RunNotify.MineSucceeded, run)
                else -> null
            }
        }
    }

    /** The runs that wait on the person now and did not before; none on the first poll, when [before] is null. */
    fun waiting(before: Set<RunRef>?, after: Set<RunRef>, runs: List<WorkflowRun>, on: Set<RunNotify>): List<RunEvent> {
        if (before == null || RunNotify.WaitsOnMe !in on) return emptyList()
        return runs.filter { it.ref in after && it.ref !in before }.map { RunEvent(RunNotify.WaitsOnMe, it) }
    }

    /** The stalls [found] in [run] not told before, for a run of the person's or a watched one. */
    fun stalled(run: WorkflowRun, found: List<RunStall>, told: List<RunStall>, facts: MineFacts, on: Set<RunNotify>): List<RunEvent> {
        if (RunNotify.MineStalled !in on || MineRules.reasons(run, facts, ALL_RULES).isEmpty()) return emptyList()
        val known = told.map { it.jobId to it.step }.toSet()
        return found.filter { (it.jobId to it.step) !in known }.map { RunEvent(RunNotify.MineStalled, run, it) }
    }
}
