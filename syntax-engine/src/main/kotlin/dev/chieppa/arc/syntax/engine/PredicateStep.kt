package dev.chieppa.arc.syntax.engine

/** One step of a query predicate such as `(#eq? @name "constructor")`. */
sealed interface PredicateStep {
    /** A capture, by its index in the query. */
    data class Capture(val index: Int) : PredicateStep

    /** A string: the predicate's name or a literal argument. */
    data class Text(val value: String) : PredicateStep

    /** The end of one predicate. */
    data object Done : PredicateStep

    companion object {
        /** tree-sitter's `TSQueryPredicateStepType` values. */
        const val TYPE_DONE = 0
        const val TYPE_CAPTURE = 1
    }
}
