package dev.chieppa.arc.index.resolve

import dev.chieppa.arc.index.symbols.DefSite
import dev.chieppa.arc.index.symbols.SymbolTable
import dev.chieppa.arc.index.symbols.UseSite
import dev.chieppa.arc.syntax.facts.SymbolKind

/**
 * Resolves a use to its candidate definitions in a [table]: the same name in the same language family (a SQL name
 * among SQL objects, columns aside), filtered by [Scope] and scored by [Ranker].
 */
class Resolver(private val table: SymbolTable) {
    fun candidates(use: UseSite): List<DefSite> {
        val all = table.definitions(use.reference.name, use.sql)
        return if (use.sql) {
            all.filter { it.definition.kind != SymbolKind.Column }
        } else {
            all.filter { it.file.grammar.family == use.file.grammar.family }
        }
    }

    fun resolve(use: UseSite): Resolution {
        val candidates = candidates(use).filterNot { it.definition.nameStart == use.reference.start && it.file.sameFile(use.file) }
        if (candidates.isEmpty()) return Resolution.None
        val (inScope, outOfScope) = candidates.partition { Scope.reachable(use, it) }
        val ranked = inScope.map { Ranked(it, Ranker.score(use, it)) }.sortedByDescending { it.score }
        val best = ranked.firstOrNull()?.score
        val confident = ranked.filter { it.score == best }
        val rest = ranked.filter { it.score != best } + outOfScope.map { Ranked(it, Ranker.score(use, it)) }.sortedByDescending { it.score }
        return Resolution(confident, rest)
    }
}
