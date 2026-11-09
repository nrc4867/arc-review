package dev.chieppa.arc.core.actions

/**
 * Sorts runs into groups: those waiting on the person, those queued, running or waiting on someone else, those failed,
 * and those done in the last day. Older finished runs that did not fail are left out. Empty groups are left out.
 */
object RunGroups {
    const val DONE_WINDOW_SECONDS = 24 * 60 * 60L

    fun of(runs: List<WorkflowRun>, waitingOnMe: Set<RunRef>, now: Long): List<GroupedRuns> {
        val byGroup = runs.mapNotNull { run -> groupOf(run, waitingOnMe, now)?.let { it to run } }.groupBy({ it.first }, { it.second })
        return RunGroup.entries.mapNotNull { group ->
            byGroup[group]?.sortedByDescending { it.createdAt }?.let { GroupedRuns(group, it) }
        }
    }

    fun groupOf(run: WorkflowRun, waitingOnMe: Set<RunRef>, now: Long): RunGroup? = when (run.state) {
        RunState.Waiting -> if (run.ref in waitingOnMe) RunGroup.WaitingOnYou else RunGroup.Running
        RunState.Queued, RunState.Running -> RunGroup.Running
        RunState.Failed -> RunGroup.Failed
        RunState.Succeeded, RunState.Cancelled, RunState.Skipped ->
            RunGroup.Done.takeIf { now - run.updatedAt.epochSecond <= DONE_WINDOW_SECONDS }
    }
}
