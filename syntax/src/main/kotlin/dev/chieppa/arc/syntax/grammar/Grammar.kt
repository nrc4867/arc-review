package dev.chieppa.arc.syntax.grammar

/**
 * One tree-sitter grammar Arc bundles: its [id] (also its query folder), the file extensions it parses, its [family],
 * and the library and entry symbol it loads from.
 */
data class Grammar(
    val id: String,
    val extensions: Set<String>,
    val family: LanguageFamily,
    val library: String = "tree-sitter-$id",
    val symbol: String = "tree_sitter_$id",
)
