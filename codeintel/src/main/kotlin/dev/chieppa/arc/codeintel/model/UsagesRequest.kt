package dev.chieppa.arc.codeintel.model

import kotlinx.serialization.Serializable

/** The usages of the name defined at [at] in [document], across the open [sources]. */
@Serializable
data class UsagesRequest(val document: DocumentRef, val at: CodePosition, val sources: List<SourceScope>)
