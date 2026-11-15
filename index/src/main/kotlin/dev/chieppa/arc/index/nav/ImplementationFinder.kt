package dev.chieppa.arc.index.nav

import dev.chieppa.arc.index.symbols.DefSite
import dev.chieppa.arc.index.symbols.SymbolTable

/**
 * What implements a type or overrides a member, by name in a [table]: types that name a supertype by its simple name,
 * in the same language family, followed down through every level.
 */
internal class ImplementationFinder(private val table: SymbolTable) {
    /** The types that extend or implement [type], directly or through others, nearest first. */
    fun subtypes(type: DefSite): List<DefSite> {
        val family = type.file.grammar.family
        val found = ArrayList<DefSite>()
        val names = ArrayDeque(listOf(type.definition.name))
        val seen = HashSet<String>().apply { add(type.definition.name) }
        while (names.isNotEmpty()) {
            for (sub in table.subtypes(names.removeFirst())) {
                if (sub.file.grammar.family != family || sub.sameAs(type) || found.any { it.sameAs(sub) }) continue
                found += sub
                if (seen.add(sub.definition.name)) names += sub.definition.name
            }
        }
        return found
    }

    /** The members of the same name and sort that override [member] in the subtypes of the type holding it. */
    fun overrides(member: DefSite): List<DefSite> {
        val definitions = member.file.facts.definitions
        val owner = definitions.getOrNull(member.definition.parent)?.takeIf { it.kind.isType } ?: return emptyList()
        return subtypes(DefSite(member.file, owner)).flatMap { sub ->
            val members = sub.file.facts.definitions
            val index = members.indexOfFirst { it.nameStart == sub.definition.nameStart }
            members.filter { it.parent == index && it.name == member.definition.name && it.kind.isCallable == member.definition.kind.isCallable }
                .map { DefSite(sub.file, it) }
        }
    }
}
