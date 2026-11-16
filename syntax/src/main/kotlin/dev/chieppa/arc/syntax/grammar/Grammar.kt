package dev.chieppa.arc.syntax.grammar

/**
 * One tree-sitter grammar Arc bundles: its [id] (also its query folder and the name the syntax engine knows it by),
 * the file extensions it parses, its [family], and the [language] id answers name it by.
 */
data class Grammar(
    val id: String,
    val extensions: Set<String>,
    val family: LanguageFamily,
    val language: String = id,
)
