package dev.chieppa.arc.core.actions

/** The notifications about runs a person can switch on or off. */
enum class RunNotify {
    /** A run started by the person or on their pull request failed. */
    MineFailed,

    /** A deployment or fork run waits for the person's approval. */
    WaitsOnMe,

    /** A watched run, or a run of a watched workflow, finished, passing or failing. */
    WatchedFinished,

    /** A run the person started succeeded. */
    MineSucceeded,

    /** A run of the person's has a step or job running far longer than usual. */
    MineStalled,
    ;

    companion object {
        /** The ones on for a first run. */
        val DEFAULTS: Set<RunNotify> = setOf(MineFailed, WaitsOnMe, WatchedFinished, MineStalled)
    }
}
