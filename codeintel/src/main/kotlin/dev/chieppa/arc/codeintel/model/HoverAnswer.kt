package dev.chieppa.arc.codeintel.model

import kotlinx.serialization.Serializable

/** Text in Markdown to show over [range], or over the word under the pointer when [range] is null, from [origin]. */
@Serializable
data class HoverAnswer(val markdown: String, val origin: AnswerOrigin, val range: CodeRange? = null)
