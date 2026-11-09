package dev.chieppa.arc.core.actions

/** How a finished run, job or step ended, as GitHub's `conclusion` says. */
enum class RunConclusion {
    Success,
    Failure,
    Cancelled,
    Skipped,
    TimedOut,
    ActionRequired,
    Neutral,
    Stale,
    StartupFailure,
}
