package dev.chieppa.arc.syntax.engine

/** A compiled query: its capture names by index, and each pattern's predicates as tree-sitter lists their steps. */
interface EngineQuery {
    val captureNames: List<String>

    val patternCount: Int

    /** Pattern [pattern]'s predicate steps, each predicate ending in [PredicateStep.Done]. */
    fun predicates(pattern: Int): List<PredicateStep>
}
