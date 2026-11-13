package dev.chieppa.arc.ui.update

/** Where an update to a newer release stands. */
sealed interface UpdateState {
    /** Nothing newer is known. */
    data object Current : UpdateState

    /** [version] is out and can be had. */
    data class Available(val version: String) : UpdateState

    /** [version] is downloading, [fraction] of it done when its size is known. */
    data class Downloading(val version: String, val fraction: Float?) : UpdateState

    /** [version] is ready and starts when Arc restarts. */
    data class Ready(val version: String) : UpdateState

    /** Getting [version] failed; it can be tried again. */
    data class Failed(val version: String) : UpdateState
}
