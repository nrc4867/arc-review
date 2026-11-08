package dev.chieppa.arc.codeintel.model

import kotlinx.serialization.Serializable

/** A Ctrl+click at [at] in [document], with the open [sources] an answer may point into. */
@Serializable
data class DefinitionRequest(val document: DocumentRef, val at: CodePosition, val sources: List<SourceScope>)
