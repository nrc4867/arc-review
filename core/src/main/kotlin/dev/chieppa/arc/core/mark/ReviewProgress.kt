package dev.chieppa.arc.core.mark

/**
 * How many of the files that count are reviewed. Files set aside do not count; files changed since they were reviewed
 * count as not reviewed.
 */
data class ReviewProgress(val reviewed: Int, val total: Int) {
    init {
        require(reviewed in 0..total) { "Reviewed $reviewed of $total" }
    }

    /** Whether every file that counts is reviewed. */
    val complete: Boolean get() = reviewed == total

    companion object {
        fun of(states: Iterable<FileReviewState>): ReviewProgress {
            val counted = states.filter { it != FileReviewState.SetAside }
            return ReviewProgress(counted.count { it == FileReviewState.Reviewed }, counted.size)
        }
    }
}
