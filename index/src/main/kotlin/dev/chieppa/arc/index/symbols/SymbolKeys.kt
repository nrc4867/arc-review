package dev.chieppa.arc.index.symbols

/**
 * The keys names are filed under. Code names keep their case; SQL names, from SQL files and from string literals, are
 * kept apart and in lower case, as T-SQL ignores case.
 */
object SymbolKeys {
    private const val SQL = "sql:"

    fun of(name: String, sql: Boolean): String = if (sql) SQL + name.lowercase() else name
}
