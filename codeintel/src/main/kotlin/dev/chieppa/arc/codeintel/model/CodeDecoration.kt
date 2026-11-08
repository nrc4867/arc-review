package dev.chieppa.arc.codeintel.model

import kotlinx.serialization.Serializable

/** A [range] of a file drawn in a [style], with optional [hover] text in Markdown, from [origin]. */
@Serializable
data class CodeDecoration(val range: CodeRange, val style: DecorationStyle, val origin: AnswerOrigin, val hover: String? = null)
