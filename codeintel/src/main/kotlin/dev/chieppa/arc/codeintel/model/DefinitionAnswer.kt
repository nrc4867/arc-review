package dev.chieppa.arc.codeintel.model

import kotlinx.serialization.Serializable

/**
 * Where the [name] clicked is defined: candidates best first, confident before filtered. [atDefinition] says the click
 * was on a definition's own name, so its usages are wanted instead.
 */
@Serializable
data class DefinitionAnswer(val name: String, val targets: List<SymbolTarget>, val atDefinition: Boolean = false)
