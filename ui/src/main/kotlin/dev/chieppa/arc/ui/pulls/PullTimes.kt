package dev.chieppa.arc.ui.pulls

/** How often GitHub is polled, as the Pull requests drawer says. */
object PullTimes {
    const val POLL_MINUTES = 5
    const val POLL_MILLIS = POLL_MINUTES * 60_000L
    const val MILLIS_PER_SECOND = 1000L
}
