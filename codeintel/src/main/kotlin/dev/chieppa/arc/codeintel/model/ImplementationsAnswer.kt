package dev.chieppa.arc.codeintel.model

import kotlinx.serialization.Serializable

/**
 * What implements [name]: for an interface, abstract class or other type, the types that extend or implement it, and
 * for a member of one, the members that override it in those types; best first, confident before filtered.
 */
@Serializable
data class ImplementationsAnswer(val name: String, val targets: List<SymbolTarget>)
