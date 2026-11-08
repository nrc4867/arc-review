package dev.chieppa.arc.syntax.facts

import dev.chieppa.arc.syntax.grammar.LanguageFamily
import dev.chieppa.arc.syntax.runtime.QueryKind
import dev.chieppa.arc.syntax.runtime.SyntaxQueries
import dev.chieppa.arc.syntax.runtime.SyntaxTree

/**
 * A file's package, imports and the SQL names inside its string literals, from its grammar's structure query. Java
 * and Kotlin imports read as dotted paths (`a.b.C`, `a.b.*`, static imports by their member); JavaScript and
 * TypeScript imports as the module text (`./ui/Button`).
 */
internal class FileStructure(val packageName: String?, val imports: List<String>, val sqlNames: List<SqlNameHit>) {
    companion object {
        private const val PACKAGE = "package"
        private const val IMPORT = "import"
        private const val STRING = "string"
        private const val STATIC = "static "
        private const val ALIAS = " as "

        fun read(tree: SyntaxTree): FileStructure {
            val query = SyntaxQueries.of(tree.grammar, QueryKind.Structure) ?: return FileStructure(null, emptyList(), emptyList())
            val names = query.captureNames
            val text = tree.text
            var packageName: String? = null
            val imports = ArrayList<String>()
            val sql = ArrayList<SqlNameHit>()
            val jvm = tree.grammar.family == LanguageFamily.Jvm
            val records = query.run(tree)
            for (i in 0 until records.size) {
                val start = records.start(i)
                val value = text.substring(start, records.end(i))
                when (names[records.capture(i)]) {
                    PACKAGE -> packageName = CodeText.squeeze(value)
                    IMPORT -> imports += if (jvm) jvmImport(value) else value
                    STRING -> SqlNames.inString(value).forEach { sql += it.copy(offset = start + it.offset) }
                }
            }
            return FileStructure(packageName, imports, sql)
        }

        private fun jvmImport(text: String): String {
            val body = CodeText.compact(text).removePrefix(IMPORT).trim().removePrefix(STATIC).substringBefore(ALIAS).removeSuffix(";")
            return CodeText.squeeze(body)
        }
    }
}
