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
        /** The state of a file whose change is now [current], given its [mark] if it has one. */
        fun of(mark: FileMark?, current: PatchHash): FileReviewState = when (mark?.kind) {
            null -> NotReviewed
            MarkKind.SetAside -> SetAside
            MarkKind.Reviewed -> if (mark.patch == current) Reviewed else ChangedSinceReviewed
        }
    }
}
