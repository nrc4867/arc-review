package dev.chieppa.arc.syntax.grammar

/**
 * Languages whose names can refer to each other's definitions: Java and Kotlin; JavaScript and TypeScript with their
 * JSX forms; T-SQL, whose names also turn up inside other languages' string literals and compare ignoring case.
 */
enum class LanguageFamily(val ignoresCase: Boolean = false) {
    Jvm,
    JavaScript,
    Sql(ignoresCase = true),
}
