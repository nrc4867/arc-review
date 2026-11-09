package dev.chieppa.arc.host.github.actions

/** What a workflow dispatch gave back: the new run's id, when GitHub returned it. */
data class DispatchResult(val runId: Long?)
