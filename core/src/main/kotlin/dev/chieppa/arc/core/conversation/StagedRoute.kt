package dev.chieppa.arc.core.conversation

/** How a staged comment goes up when the review is pushed. */
enum class StagedRoute {
    /** As a comment on its line. */
    Line,

    /** In the review's summary text, with a link to the line, where the host takes no comment on it. */
    Summary,
}
