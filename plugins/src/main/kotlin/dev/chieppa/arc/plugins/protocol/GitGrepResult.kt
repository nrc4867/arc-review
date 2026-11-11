package dev.chieppa.arc.plugins.protocol

import kotlinx.serialization.Serializable

/** What a search found. */
@Serializable
data class GitGrepResult(val hits: List<GrepHit>)
