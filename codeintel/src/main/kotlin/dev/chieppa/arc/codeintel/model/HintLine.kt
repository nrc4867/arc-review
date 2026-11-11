package dev.chieppa.arc.codeintel.model

import kotlinx.serialization.Serializable

/** The [text] of a hint written on [line] of its file, from 1. */
@Serializable
data class HintLine(val text: String, val line: Int)
