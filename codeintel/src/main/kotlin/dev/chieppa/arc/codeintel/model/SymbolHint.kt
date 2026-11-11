package dev.chieppa.arc.codeintel.model

import kotlinx.serialization.Serializable

/**
 * One definition as a symbol card shows it: the [target] it is at, the [container] around it with its [containerKind]
 * (as in [SymbolTarget.kind]), its [signature] as written, one part per line of the file, and its [doc] comment.
 */
@Serializable
data class SymbolHint(
    val target: SymbolTarget,
    val signature: List<HintLine>,
    val container: String? = null,
    val containerKind: String? = null,
    val doc: HintDoc? = null,
)
