package dev.chieppa.arc.index.resolve

import dev.chieppa.arc.index.symbols.DefSite
import dev.chieppa.arc.index.symbols.UseSite
import dev.chieppa.arc.syntax.facts.RefKind

/**
 * Scores a candidate definition for a use from what the index holds: same file, imported or same package, same repo,
 * the kind of use against the kind of definition, argument count against parameters, main code over test code, and a
 * SQL name's schema.
 */
object Ranker {
    fun score(use: UseSite, candidate: DefSite): Int {
        var score = 0
        val definition = candidate.definition
        if (candidate.file.sameFile(use.file)) score += ScoreWeights.SAME_FILE
        if (candidate.file.repo == use.file.repo) score += ScoreWeights.SAME_REPO
        score += importScore(use, candidate)
        if (!TestPaths.isTest(candidate.file.path)) score += ScoreWeights.MAIN_OVER_TEST
        when (use.reference.kind) {
            RefKind.Call -> score += if (definition.kind.isCallable || definition.kind.isType) ScoreWeights.KIND_MATCH else ScoreWeights.KIND_MISMATCH
            RefKind.Type -> score += if (definition.kind.isType) ScoreWeights.KIND_MATCH else ScoreWeights.KIND_MISMATCH
            RefKind.SqlString, RefKind.Name -> Unit
        }
        val args = use.reference.arity
        val params = definition.arity
        if (args != null && params != null) {
            score += when {
                args == params -> ScoreWeights.ARITY_MATCH
                args < params -> ScoreWeights.ARITY_FITS
                else -> ScoreWeights.ARITY_MISMATCH
            }
        }
        val schema = use.reference.schema
        if (use.sql && schema != null && schema == definition.container) score += ScoreWeights.SQL_SCHEMA
        return score
    }

    private fun importScore(use: UseSite, candidate: DefSite): Int {
        val definition = candidate.definition
        val pkg = use.file.facts.packageName
        val defPkg = candidate.file.facts.packageName
        val importable = if (definition.containerKind?.isType == true) definition.container else definition.name
        val imports = use.file.facts.imports
        return when {
            defPkg != null && imports.any { it == "$defPkg.$importable" } -> ScoreWeights.IMPORTED
            defPkg != null && defPkg == pkg && !candidate.file.sameFile(use.file) -> ScoreWeights.SAME_PACKAGE
            defPkg != null && imports.any { it == "$defPkg.*" } -> ScoreWeights.WILDCARD_IMPORT
            imports.any { ModulePaths.resolvesTo(use.file.path, it, candidate.file.path) } -> ScoreWeights.IMPORTED
            else -> 0
        }
    }
}
