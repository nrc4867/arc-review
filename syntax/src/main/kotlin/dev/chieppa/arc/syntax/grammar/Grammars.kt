package dev.chieppa.arc.syntax.grammar

/** The grammars Arc bundles, and which one parses a path. */
object Grammars {
    val java = Grammar("java", setOf("java"), LanguageFamily.Jvm)
    val kotlin = Grammar("kotlin", setOf("kt", "kts"), LanguageFamily.Jvm)
    val javascript = Grammar("javascript", setOf("js", "jsx", "mjs", "cjs"), LanguageFamily.JavaScript)
    val typescript = Grammar("typescript", setOf("ts", "mts", "cts"), LanguageFamily.JavaScript)
    val tsx = Grammar("tsx", setOf("tsx"), LanguageFamily.JavaScript)
    val mssql = Grammar("mssql", setOf("sql"), LanguageFamily.Sql, language = "tsql")

    val all: List<Grammar> = listOf(java, kotlin, javascript, typescript, tsx, mssql)

    private val byExtension = all.flatMap { grammar -> grammar.extensions.map { it to grammar } }.toMap()
    private val byId = all.associateBy { it.id }

    /** The grammar for [path] by its extension, or null when Arc has none. */
    fun forPath(path: String): Grammar? =
        byExtension[path.substringAfterLast('/').substringAfterLast('.', "").lowercase()]

    /** The grammar named [id], or null. */
    fun byId(id: String): Grammar? = byId[id]
}
