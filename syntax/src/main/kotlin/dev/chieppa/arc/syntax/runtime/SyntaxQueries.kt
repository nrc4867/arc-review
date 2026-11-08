package dev.chieppa.arc.syntax.runtime

import dev.chieppa.arc.syntax.grammar.Grammar
import java.util.Optional
import java.util.concurrent.ConcurrentHashMap

/** Each grammar's queries, compiled on first use and shared. A grammar without a query file of a kind has no query. */
object SyntaxQueries {
    private const val ROOT = "/dev/chieppa/arc/syntax/queries"

    /** Captures every ERROR node, for judging how much of a file failed to parse. */
    private const val ERRORS = "(ERROR) @error"

    private val compiled = ConcurrentHashMap<Pair<String, QueryKind>, Optional<SyntaxQuery>>()
    private val errors = ConcurrentHashMap<String, SyntaxQuery>()

    /** [grammar]'s query of [kind], or null when it has none. */
    fun of(grammar: Grammar, kind: QueryKind): SyntaxQuery? = compiled.computeIfAbsent(grammar.id to kind) {
        Optional.ofNullable(source(grammar, kind)?.let { SyntaxQuery.compile(grammar, it) })
    }.orElse(null)

    /** The query capturing [grammar]'s ERROR nodes. */
    fun errors(grammar: Grammar): SyntaxQuery = errors.computeIfAbsent(grammar.id) { SyntaxQuery.compile(grammar, ERRORS) }

    /** The text of [grammar]'s query file of [kind], or null. */
    fun source(grammar: Grammar, kind: QueryKind): String? =
        SyntaxQueries::class.java.getResource("$ROOT/${grammar.id}/${kind.file}")?.readText()
}
