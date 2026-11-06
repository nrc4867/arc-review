package dev.chieppa.arc.core.mark

/**
 * What replaying the change reviewed at [reviewed] onto the source's new merge-base gave: the paths that [conflicted],
 * and the paths whose replayed file [differs] from the file at the new head.
 */
data class ReplayOutcome(
    val reviewed: ReviewedRevisions,
    val conflicted: Set<String>,
    val differs: Set<String>,
)
