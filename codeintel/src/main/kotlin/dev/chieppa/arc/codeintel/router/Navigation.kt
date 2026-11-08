package dev.chieppa.arc.codeintel.router

import dev.chieppa.arc.codeintel.model.DefinitionAnswer
import dev.chieppa.arc.codeintel.model.SymbolTarget
import dev.chieppa.arc.codeintel.model.UsagesAnswer

/** What a Ctrl+click leads to. */
sealed interface Navigation {
    /** One confident definition: go there. */
    data class Jump(val target: SymbolTarget) : Navigation

    /** Several candidates, or only filtered ones: list them, confident first and filtered dimmed below. */
    data class Choose(val answer: DefinitionAnswer) : Navigation

    /** The click was on a definition: list its usages. */
    data class Usages(val answer: UsagesAnswer) : Navigation

    /** Nothing to show, as for a library call with no source here. */
    data object Nothing : Navigation
}
