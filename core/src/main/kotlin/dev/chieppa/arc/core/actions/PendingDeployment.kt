package dev.chieppa.arc.core.actions

import java.time.Instant

/**
 * An environment a run waits at: its id and name, since when, whether the person may approve it, and the people and
 * teams who may.
 */
data class PendingDeployment(
    val environmentId: Long,
    val environment: String,
    val waitingSince: Instant?,
    val canApprove: Boolean,
    val reviewers: List<String> = emptyList(),
)
