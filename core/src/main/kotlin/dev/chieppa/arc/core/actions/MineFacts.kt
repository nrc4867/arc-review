package dev.chieppa.arc.core.actions

/**
 * What deciding whether a run is the person's needs: their [login], their open pull requests, the runs that wait on
 * them, and what they watch.
 */
data class MineFacts(
    val login: String?,
    val pulls: List<MyPull> = emptyList(),
    val waitingOnMe: Set<RunRef> = emptySet(),
    val workflows: List<WatchedWorkflow> = emptyList(),
    val runs: Set<RunRef> = emptySet(),
)
