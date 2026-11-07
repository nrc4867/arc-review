package dev.chieppa.arc.host.github.pulls

/** Who or what a pull request waits on next. */
sealed interface WaitingOn {
    /** The person's review; [again] when they reviewed before and commits came after. */
    data class You(val again: Boolean) : WaitingOn

    /** The checks: [check] is the first that failed, or null when the rollup failed without naming one. */
    data class Ci(val check: String?) : WaitingOn

    /** The author, with [threads] review threads still open, or changes asked for. */
    data class Author(val threads: Int) : WaitingOn

    /** Approved and mergeable: someone to merge it. */
    data object Merge : WaitingOn

    /** [count] reviewers asked and not answered yet. */
    data class Reviewers(val count: Int) : WaitingOn
}
