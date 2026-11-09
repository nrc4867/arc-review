package dev.chieppa.arc.core.actions

/** One group of a run list, its runs newest first. */
data class GroupedRuns(val group: RunGroup, val runs: List<WorkflowRun>)
