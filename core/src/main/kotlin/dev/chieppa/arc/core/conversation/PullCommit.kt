package dev.chieppa.arc.core.conversation

/**
 * A commit on a pull request's branch as its conversation lists it: its [sha], the first line of its message
 * ([headline]), who wrote it, with their picture's address where the host gives one, and when it was committed (epoch
 * seconds).
 */
data class PullCommit(val sha: String, val headline: String, val author: String, val committedAt: Long, val avatar: String? = null)
