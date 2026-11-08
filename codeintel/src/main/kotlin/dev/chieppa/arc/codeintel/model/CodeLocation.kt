package dev.chieppa.arc.codeintel.model

import kotlinx.serialization.Serializable

/** A [range] in a [document]. */
@Serializable
data class CodeLocation(val document: DocumentRef, val range: CodeRange)
