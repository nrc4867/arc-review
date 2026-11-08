package dev.chieppa.arc.core.conversation

/** Where a thread on a line stands. */
enum class ThreadState {
    Open,

    /** Still open on the host, but its lines changed in a later revision. */
    Addressed,
    Resolved,

    /** The person's own comment, not yet staged. */
    Draft,

    /** The person's own comment, staged to go up with the review. */
    Staged,

    /** The person's own note on the line, kept on this machine. */
    Note,

    /** Left by an outside agent; a person adopts or dismisses it. */
    Guest,
}
