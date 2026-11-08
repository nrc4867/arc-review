package dev.chieppa.arc.index.symbols

import dev.chieppa.arc.index.IndexedFile
import dev.chieppa.arc.syntax.grammar.LanguageFamily

/** Every definition and use in a set of files, of any repos and revisions, by name. */
class SymbolTable(files: Sequence<IndexedFile>) {
    private val definitions = HashMap<String, MutableList<DefSite>>()
    private val uses = HashMap<String, MutableList<UseSite>>()

    init {
        for (file in files) {
            val sqlFile = file.grammar.family == LanguageFamily.Sql
            for (definition in file.facts.definitions) {
                definitions.getOrPut(SymbolKeys.of(definition.name, sqlFile)) { ArrayList() } += DefSite(file, definition)
            }
            for (reference in file.facts.references) {
                val site = UseSite(file, reference)
                uses.getOrPut(SymbolKeys.of(reference.name, site.sql)) { ArrayList() } += site
            }
        }
    }

    /** The definitions filed under [name], SQL ones when [sql]. */
    fun definitions(name: String, sql: Boolean): List<DefSite> = definitions[SymbolKeys.of(name, sql)].orEmpty()

    /** The uses filed under [name], SQL ones when [sql]. */
    fun uses(name: String, sql: Boolean): List<UseSite> = uses[SymbolKeys.of(name, sql)].orEmpty()

    companion object {
        val Empty = SymbolTable(emptySequence())
    }
}
