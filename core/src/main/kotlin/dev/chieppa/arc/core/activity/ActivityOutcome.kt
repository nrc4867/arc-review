package dev.chieppa.arc.core.activity

/** How an operation in the activity log ended. */
enum class ActivityOutcome {
    Succeeded,
    Failed,

    /** A warning a plugin wrote: not a failure, but worth a look. */
    Warned,

    /** A long-running process, recorded once when it starts. */
    Started,

    /** Stopped by Arc before it finished, as when the work asking for it went away. */
    Cancelled,
}
