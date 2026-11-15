package dev.chieppa.arc.codeintel.model

import kotlinx.serialization.Serializable

/**
 * Go to implementation at [at] in [document], on a type or a member's name or a use of one, with the open [sources] an
 * answer may point into.
 */
@Serializable
data class ImplementationsRequest(val document: DocumentRef, val at: CodePosition, val sources: List<SourceScope>)
