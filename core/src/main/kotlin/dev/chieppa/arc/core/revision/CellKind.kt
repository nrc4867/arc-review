package dev.chieppa.arc.core.revision

/** What happened to a file in one revision, as its cell in the revision matrix shows it. */
enum class CellKind {
    /** Its change is the same as in the revision before. */
    Unchanged,

    /** Its change differs from the revision before, and it was not reviewed since. */
    Changed,

    /** Changed here, and covered by a reviewed mark made here or later, or by one that still holds. */
    Reviewed,

    /** Changed here, after the revision its reviewed mark was made at. */
    ChangedAfterReview,
    ;

    val changed: Boolean get() = this != Unchanged
}
