package dev.chieppa.arc.index.resolve

import dev.chieppa.arc.index.symbols.DefSite
import dev.chieppa.arc.index.symbols.UseSite
import dev.chieppa.arc.syntax.facts.RefKind
import dev.chieppa.arc.syntax.grammar.LanguageFamily

/**
 * Whether a definition could be what a use means under Java and Kotlin's visibility, judged by names: a call needs a
 * callable or a type; a local definition is seen only in its own file; main code does not see test code; a member of
 * another file's type is reached only through a receiver; a name from another package must be imported or share the
 * package. Other languages pass everything, leaving the choice to the score.
 */
object Scope {
    fun reachable(use: UseSite, candidate: DefSite): Boolean {
        if (use.file.grammar.family != LanguageFamily.Jvm || use.sql) return true
        val definition = candidate.definition
        if (use.reference.kind == RefKind.Call && !(definition.kind.isCallable || definition.kind.isType)) return false
        if (candidate.file.sameFile(use.file)) return true
        if (!TestPaths.isTest(use.file.path) && TestPaths.isTest(candidate.file.path)) return false
        if (definition.isLocal) return false
        val member = definition.containerKind?.isType == true
        if (member && !use.reference.qualified) return false
        val importable = if (member) definition.container!! else definition.name
        return visible(use, candidate, importable)
    }

    private fun visible(use: UseSite, candidate: DefSite, name: String): Boolean {
        val pkg = candidate.file.facts.packageName
        val imports = use.file.facts.imports
        return pkg == use.file.facts.packageName || imports.any { it == "$pkg.$name" || it == "$pkg.*" }
    }
}
