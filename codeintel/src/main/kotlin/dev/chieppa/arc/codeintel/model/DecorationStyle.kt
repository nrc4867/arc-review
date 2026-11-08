package dev.chieppa.arc.codeintel.model

import kotlinx.serialization.Serializable

/** The themed looks a decoration can take; the viewer picks the colours. */
@Serializable
enum class DecorationStyle { Highlight, Underline, Info, Warning, Error, Muted }
