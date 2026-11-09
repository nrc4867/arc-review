package dev.chieppa.arc.ui.runs

/** How often runs are polled. */
object RunTimes {
    /** A repo's run list, while it has had a run in the last day. */
    const val ACTIVE_MILLIS = 60_000L

    /** A repo's run list once it has had no run for a day. */
    const val QUIET_MILLIS = 5 * 60_000L
    const val QUIET_AFTER_SECONDS = 24 * 60 * 60L

    /** A run's jobs while its page shows. */
    const val OPEN_MILLIS = 10_000L

    /** A run's jobs while it is only watched, or the person's own and running. */
    const val WATCHED_MILLIS = 30_000L

    /** How often the lists loop wakes to see which repos are due. */
    const val TICK_MILLIS = 10_000L

    /** Watched runs are forgotten this long after they were watched. */
    const val FORGET_WATCHED_MILLIS = 30L * 24 * 60 * 60 * 1000

    const val MILLIS_PER_SECOND = 1000L
}
