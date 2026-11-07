package dev.chieppa.arc.ui.pulls

/** How polling GitHub stands. */
enum class WatchStatus {
    /** Not polled yet. */
    Waiting,

    /** The last poll answered. */
    Live,

    /** No token, or GitHub refused it: nothing can be polled. */
    Unavailable,

    /** The last poll failed, such as with no network; the results before it stay. */
    Failed,
}
