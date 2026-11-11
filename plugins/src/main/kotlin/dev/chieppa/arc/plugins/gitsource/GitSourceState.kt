package dev.chieppa.arc.plugins.gitsource

/**
 * Where a git plugin source stands: when it was last [checkedAt] for new commits (epoch milliseconds), the [update]
 * waiting for the person, whether the last clone, check or move [failed], and whether one is under way ([busy]).
 */
data class GitSourceState(
    val checkedAt: Long? = null,
    val update: GitUpdate? = null,
    val failed: Boolean = false,
    val busy: Boolean = false,
)

