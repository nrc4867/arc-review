package dev.chieppa.arc.host.github.pulls

/** Whether a pull request is open, merged or closed without merging. */
enum class PullState {
    Open,
    Merged,
    Closed,
    ;

    /** Whether it is merged or closed. */
    val ended: Boolean get() = this != Open

    companion object {
        /** GitHub's name for a pull request's state, such as `MERGED`; open for anything else. */
        fun parse(name: String?): PullState = when (name) {
            MERGED -> Merged
            CLOSED -> Closed
            else -> Open
        }

        private const val MERGED = "MERGED"
        private const val CLOSED = "CLOSED"
    }
}
