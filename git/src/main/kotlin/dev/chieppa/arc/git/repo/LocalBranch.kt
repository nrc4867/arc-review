package dev.chieppa.arc.git.repo

/** A branch in a clone: its [name], who made its last commit, and when it was committed, in epoch seconds. */
data class LocalBranch(val name: String, val author: String, val committed: Long)
