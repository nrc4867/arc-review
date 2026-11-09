package dev.chieppa.arc.host.github.actions

/** What a fork's pull request [pull] changes under `.github/`, the code that runs with the repo's permissions. */
data class ForkChanges(val pull: Int, val changes: List<ForkChange>)
