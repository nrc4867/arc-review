package dev.chieppa.arc.git.repo

/**
 * A branch in a clone: its [name], who made its last commit, when it was committed, in epoch seconds, and how many
 * commits it has that the repo's default branch does not ([ahead]), where git can tell.
 */
data class LocalBranch(val name: String, val author: String, val committed: Long, val ahead: Int? = null)
