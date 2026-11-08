package dev.chieppa.arc.codeintel.model

import kotlinx.serialization.Serializable

/** The decorations wanted on [document], shown as part of [source] when it is one of the open sources. */
@Serializable
data class DecorationRequest(val document: DocumentRef, val source: SourceScope? = null)
