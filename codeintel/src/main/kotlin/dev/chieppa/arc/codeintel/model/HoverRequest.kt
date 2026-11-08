package dev.chieppa.arc.codeintel.model

import kotlinx.serialization.Serializable

/** The pointer rests at [at] in [document]. */
@Serializable
data class HoverRequest(val document: DocumentRef, val at: CodePosition, val sources: List<SourceScope> = emptyList())
