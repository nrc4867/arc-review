package dev.chieppa.arc.syntax.runtime

import dev.chieppa.arc.syntax.engine.EngineQuery
import dev.chieppa.arc.syntax.engine.PredicateStep

/** Reads a compiled query's predicates, pattern by pattern, into [QueryPredicate]s. */
internal object QueryPredicates {
    private const val ANY_OF = "any-of?"
    private const val NOT_ANY_OF = "not-any-of?"

    fun read(query: EngineQuery): List<List<QueryPredicate>> = (0 until query.patternCount).map { pattern ->
        val predicates = ArrayList<QueryPredicate>()
        var current = ArrayList<PredicateStep>()
        for (step in query.predicates(pattern)) {
            if (step == PredicateStep.Done) {
                parse(current)?.let(predicates::add)
                current = ArrayList()
            } else {
                current += step
            }
        }
        predicates
    }

    private fun parse(steps: List<PredicateStep>): QueryPredicate? {
        val name = (steps.firstOrNull() as? PredicateStep.Text)?.value ?: return null
        val capture = (steps.getOrNull(1) as? PredicateStep.Capture)?.index ?: return null
        val rest = steps.drop(2)
        val strings = rest.mapNotNull { (it as? PredicateStep.Text)?.value }
        if (name == ANY_OF || name == NOT_ANY_OF) return QueryPredicate.AnyOf(capture, strings.toSet(), name == NOT_ANY_OF)
        val negate = name.removePrefix("any-").startsWith("not-")
        val any = name.startsWith("any-")
        val first = rest.firstOrNull()
        val text = (first as? PredicateStep.Text)?.value
        return when (name.removePrefix("any-").removePrefix("not-")) {
            "eq?" -> QueryPredicate.Equals(capture, (first as? PredicateStep.Capture)?.index, text, negate, any)
            "match?" -> text?.let { QueryPredicate.Matches(capture, Regex(it), negate, any) }
            "lua-match?" -> text?.let { QueryPredicate.Matches(capture, Regex(LuaPatterns.toRegex(it)), negate, any) }
            else -> null
        }
    }
}
