package dev.chieppa.arc.host.github.history

/**
 * What GitHub knows of the heads a pull request has had: its [head] now, its [forcePushes] oldest first, and the
 * commits its [reviews] were made on. GitHub keeps no record of an ordinary push, so heads between force pushes that
 * nobody reviewed are not here.
 */
data class PullHistory(val head: String?, val forcePushes: List<ForcePush> = emptyList(), val reviews: List<String> = emptyList())
