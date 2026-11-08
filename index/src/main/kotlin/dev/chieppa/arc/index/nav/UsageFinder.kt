package dev.chieppa.arc.index.nav

import dev.chieppa.arc.codeintel.model.Confidence
import dev.chieppa.arc.index.resolve.Resolver
import dev.chieppa.arc.index.symbols.DefSite
import dev.chieppa.arc.index.symbols.SymbolTable
import dev.chieppa.arc.index.symbols.UseSite
import dev.chieppa.arc.syntax.facts.RefKind
import dev.chieppa.arc.syntax.grammar.LanguageFamily

/**
 * The usages of a definition in a [table]. A use in the same language family counts when it resolves to the
 * definition: confidently, or as filtered when nothing in scope claims it. A SQL object's uses, in SQL files and in
 * other languages' strings, count unless their schema names another. A type also collects its name in other language
 * families, uses and definitions alike, as a type mirrored by hand across a boundary.
 */
class UsageFinder(private val table: SymbolTable) {
    private val resolver = Resolver(table)

    fun usages(site: DefSite): List<FoundUsage> {
        val definition = site.definition
        val family = site.file.grammar.family
        if (family == LanguageFamily.Sql) return sqlUsages(site)
        val indexed = table.definitions(definition.name, false).any { it.sameAs(site) }
        val found = ArrayList<FoundUsage>()
        for (use in table.uses(definition.name, false)) {
            if (use.file.grammar.family == family) {
                val resolution = resolver.resolve(use)
                val confidence = when {
                    !indexed -> Confidence.Filtered
                    resolution.isConfident(site) -> Confidence.Confident
                    resolution.confident.isEmpty() && resolution.mentions(site) -> Confidence.Filtered
                    else -> null
                } ?: continue
                found += usage(use, confidence)
            } else if (definition.kind.isType) {
                found += usage(use, Confidence.Confident)
            }
        }
        if (definition.kind.isType) {
            for (other in table.definitions(definition.name, false)) {
                if (other.file.grammar.family != family && other.definition.kind.isType) found += definitionUsage(other)
            }
        }
        return found
    }

    private fun sqlUsages(site: DefSite): List<FoundUsage> {
        val schema = site.definition.container
        return table.uses(site.definition.name, true).map { use ->
            val written = use.reference.schema
            usage(use, if (written == null || schema == null || written == schema) Confidence.Confident else Confidence.Filtered)
        }
    }

    private fun usage(use: UseSite, confidence: Confidence): FoundUsage {
        val reference = use.reference
        return FoundUsage(use.file, reference.line, reference.column, reference.end - reference.start, reference.name, kindOf(reference.kind), confidence)
    }

    private fun definitionUsage(other: DefSite): FoundUsage {
        val definition = other.definition
        return FoundUsage(
            other.file, definition.line, definition.column, definition.nameEnd - definition.nameStart, definition.label, definition.kind.tag, Confidence.Confident,
        )
    }

    private fun kindOf(kind: RefKind): String = when (kind) {
        RefKind.Call -> UseKinds.CALL
        RefKind.Type -> UseKinds.TYPE
        RefKind.Name -> UseKinds.NAME
        RefKind.SqlString -> UseKinds.SQL
    }
}
