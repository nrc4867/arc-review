package dev.chieppa.arc.index.symbols

import dev.chieppa.arc.index.IndexedFile
import dev.chieppa.arc.syntax.facts.RefKind
import dev.chieppa.arc.syntax.facts.Reference
import dev.chieppa.arc.syntax.grammar.LanguageFamily

/** A [reference] in a [file]. */
data class UseSite(val file: IndexedFile, val reference: Reference) {
    /** Names a SQL object: in a SQL file, or inside a string literal. */
    val sql: Boolean get() = file.grammar.family == LanguageFamily.Sql || reference.kind == RefKind.SqlString
}
