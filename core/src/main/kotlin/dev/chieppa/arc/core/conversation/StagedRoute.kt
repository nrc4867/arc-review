package dev.chieppa.arc.core.conversation

/** How a staged comment goes up when the review is pushed. */
enum class StagedRoute {
    /** As a comment on its line. */
    Line,

    /** As a comment on its file that quotes the line and links to it, where the host takes no comment on that line. */
    File,

    /** In the review's summary text, with a link to the line, where the host takes no comment on its file. */
    Summary,

    /** As a reply under a thread already on the host. */
    Reply,

    /** As a comment on the pull request's conversation. */
    Conversation,

    /** As a comment on a commit, for a source that is not a pull request. */
    Commit,
}
