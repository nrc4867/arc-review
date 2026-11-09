package dev.chieppa.arc.core.actions

/** Where a run, job or step is in its life, as GitHub's `status` says. */
enum class RunStatus {
    Requested,
    Queued,
    Pending,
    Waiting,
    InProgress,
    Completed,
}
