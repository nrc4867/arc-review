package dev.chieppa.arc.core.actions

import java.time.Instant

/** A file or zip a run uploaded: its [name], [sizeBytes], and when GitHub deletes it. */
data class RunArtifact(
    val id: Long,
    val name: String,
    val sizeBytes: Long,
    val expiresAt: Instant?,
    val expired: Boolean,
    val createdAt: Instant?,
)
