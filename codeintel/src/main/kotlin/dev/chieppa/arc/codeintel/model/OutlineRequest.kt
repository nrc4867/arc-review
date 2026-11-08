package dev.chieppa.arc.codeintel.model

import kotlinx.serialization.Serializable

/** The outline wanted for [document]. */
@Serializable
data class OutlineRequest(val document: DocumentRef)
