package dev.chieppa.arc.plugins.host

/** The exit codes a worker gets for ends that are not its own. */
object WorkerExits {
    /** The shared Node process ended. */
    const val HOST_ENDED = -1

    /** The shared Node process started again, to read another plugin's folder; the worker starts again at once. */
    const val HOST_RESTARTED = -2

    /** Arc stopped it, or started another in its place. */
    const val STOPPED = -3
}
