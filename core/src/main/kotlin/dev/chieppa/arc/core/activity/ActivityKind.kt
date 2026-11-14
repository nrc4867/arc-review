package dev.chieppa.arc.core.activity

/** What sort of operation an [ActivityEntry] records. */
enum class ActivityKind {
    /** A git command. */
    Git,

    /** A request to GitHub's API. */
    GitHub,

    /** A file downloaded, such as a pinned runtime. */
    Download,

    /** Arc updating itself: checking for a release and fetching it. */
    Update,

    /** Text a plugin wrote to its own channel. */
    Output,
}
