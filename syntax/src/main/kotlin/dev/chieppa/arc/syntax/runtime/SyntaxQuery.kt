package dev.chieppa.arc.syntax.runtime

import dev.chieppa.arc.syntax.engine.EngineQuery
import dev.chieppa.arc.syntax.engine.SyntaxEngines
import dev.chieppa.arc.syntax.grammar.Grammar

/**
 * A compiled tree-sitter query for one grammar. Compiled queries are immutable and shared by every thread; they live
 * as long as the process. [run] executes it in the syntax engine in one call and applies its text predicates.
 */
class SyntaxQuery private constructor(val grammar: Grammar, private val query: EngineQuery) {
    /** The query's capture names, by capture index. */
    val captureNames: List<String> get() = query.captureNames

    private val predicates: List<List<QueryPredicate>> = QueryPredicates.read(query)
    private val hasPredicates: Boolean = predicates.any { it.isNotEmpty() }

    /** The index of capture [name], or -1 when the query has none by that name. */
    fun captureIndex(name: String): Int = captureNames.indexOf(name)

    /** Every capture of every match in [tree] that passes the query's predicates. */
    fun run(tree: SyntaxTree): QueryRecords {
        val records = QueryRecords(tree.tree.captures(query))
        if (!hasPredicates) return records
        return records.filterMatches { from, until ->
            val tests = predicates[records.pattern(from)]
            tests.isEmpty() || tests.all { predicate ->
                predicate.test { capture ->
                    (from until until).filter { records.capture(it) == capture }.map { tree.text.substring(records.start(it), records.end(it)) }
                }
            }
        }
    }

    companion object {
        /** Compiles [source] for [grammar]; throws `QueryException` naming where it fails. */
        fun compile(grammar: Grammar, source: String): SyntaxQuery = SyntaxQuery(grammar, SyntaxEngines.current.compile(grammar.id, source))
    }
}
