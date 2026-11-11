package dev.chieppa.arc.codeintel.model

import kotlinx.serialization.Serializable

/**
 * Text in Markdown to show over [range], or over the word under the pointer when [range] is null, from [origin]. A
 * provider that knows the definitions behind the name adds them as [symbols], best first, which a symbol card shows
 * one at a time in place of [markdown].
 */
@Serializable
data class HoverAnswer(
    val markdown: String,
    val origin: AnswerOrigin,
    val range: CodeRange? = null,
    val symbols: List<SymbolHint> = emptyList(),
)
