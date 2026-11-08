package dev.chieppa.arc.codeintel.model

import kotlinx.serialization.Serializable

/**
 * One entry of a file's outline: its [name] with the [parameters] it takes, its [kind] (as in [SymbolTarget]), its
 * [type] as written, where its name is, and the entries inside it.
 */
@Serializable
data class OutlineSymbol(
    val name: String,
    val kind: String,
    val range: CodeRange,
    val parameters: List<String>? = null,
    val type: String? = null,
    val children: List<OutlineSymbol> = emptyList(),
)
