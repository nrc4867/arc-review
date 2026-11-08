package dev.chieppa.arc.codeintel.model

import kotlinx.serialization.Serializable

/**
 * One place an answer points at: the [location] of a name, the [name] as written there, its [kind] (`class`,
 * `function`, `property`, `procedure`, `table` and so on for a definition; `call`, `type`, `name` or `sql` for a use),
 * the line's text as [preview], how sure the provider is, and where the answer came from. [source] is the ref of the
 * open source the location was read in, if any.
 */
@Serializable
data class SymbolTarget(
    val location: CodeLocation,
    val name: String,
    val kind: String,
    val preview: String,
    val confidence: Confidence,
    val origin: AnswerOrigin,
    val source: String? = null,
)
