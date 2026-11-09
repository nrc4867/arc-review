package dev.chieppa.arc.core.actions

/** One state for a run, job or step out of its status and conclusion, as the app shows it. */
enum class RunState {
    /** Held at a deployment gate or for a maintainer's approval. */
    Waiting,
    Queued,
    Running,
    Succeeded,
    Failed,
    Cancelled,
    Skipped,
    ;

    val finished: Boolean get() = this == Succeeded || this == Failed || this == Cancelled || this == Skipped

    companion object {
        fun of(status: RunStatus, conclusion: RunConclusion?): RunState = when (status) {
            RunStatus.Waiting -> Waiting
            RunStatus.Requested, RunStatus.Queued, RunStatus.Pending -> Queued
            RunStatus.InProgress -> Running
            RunStatus.Completed -> when (conclusion) {
                RunConclusion.Success, RunConclusion.Neutral -> Succeeded
                RunConclusion.Cancelled, RunConclusion.Stale -> Cancelled
                RunConclusion.Skipped -> Skipped
                RunConclusion.ActionRequired -> Waiting
                RunConclusion.Failure, RunConclusion.TimedOut, RunConclusion.StartupFailure, null -> Failed
            }
        }
    }
}
