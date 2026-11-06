package dev.chieppa.arc.core.mark

/** Where a changed file stands in a review, as its row in the tree shows it. */
enum class FileReviewState {
    NotReviewed,
    Reviewed,

    /** Reviewed, but its change has differed since. */
    ChangedSinceReviewed,
    SetAside,
    ;

    companion object {
        /** The state of a file with [mark], if it has one, whose reviewed mark got [verdict]. */
        fun of(mark: FileMark?, verdict: MarkVerdict): FileReviewState = when (mark?.kind) {
            null -> NotReviewed
            MarkKind.SetAside -> SetAside
            MarkKind.Reviewed -> if (verdict == MarkVerdict.Keep) Reviewed else ChangedSinceReviewed
        }

        /** The state of a file with [mark] whose change is now [current], deciding as [MarkRules] does. */
        fun of(mark: FileMark?, current: PatchFingerprint?, replay: ReplayOutcome? = null): FileReviewState =
            of(mark, mark?.let { MarkRules.verdict(it, current, replay) } ?: MarkVerdict.Clear)
    }
}
