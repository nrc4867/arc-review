package dev.chieppa.arc.syntax.facts

/** A SQL object name found at [offset] in some text: its lower-case [name] and written [schema]. */
data class SqlNameHit(val name: String, val schema: String?, val offset: Int, val length: Int)

/**
 * SQL object names: normalising `[sales].[usp_PlaceOrder]` to schema `sales` and name `usp_placeorder`, and finding
 * the names a string literal in another language refers to (`{call sales.usp_PlaceOrder(?)}`, `EXEC [sales].[x]`,
 * `FROM sales.Customer`, `sales.ufn_Balance(?)`). A string counts only when it reads as SQL.
 */
object SqlNames {
    private const val PART = """(?:\[[^\]]+\]|"[^"]+"|[A-Za-z_][\w$#]*)"""
    private const val NAME = """$PART(?:\s*\.\s*$PART){0,3}"""
    private val afterKeyword =
        Regex("""(?i)\b(?:exec(?:ute)?|call|from|join|into|update|merge(?:\s+into)?|delete\s+from|truncate\s+table)\s+($NAME)""")
    private val qualifiedCall = Regex("""($PART\s*\.\s*$PART)\s*\(""")
    private val sqlish = Regex("""(?i)\b(select|insert|update|delete|merge|exec|execute|call)\b""")
    private val reserved = setOf("select", "set", "where", "values", "with", "as", "on", "openjson", "openquery", "openrowset")

    /** Schema (when written) and lower-case name of a possibly bracketed, possibly multi-part SQL name. */
    fun normalize(raw: String): Pair<String?, String> {
        val parts = raw.split('.').map { it.trim().removeSurrounding("[", "]").removeSurrounding("\"").lowercase() }.filter { it.isNotEmpty() }
        val name = parts.lastOrNull() ?: ""
        val schema = if (parts.size >= 2) parts[parts.size - 2] else null
        return schema to name
    }

    /** The SQL object names [text], a string literal's content, refers to. Empty unless it reads as SQL. */
    fun inString(text: String): List<SqlNameHit> {
        if (!sqlish.containsMatchIn(text)) return emptyList()
        val hits = LinkedHashMap<Int, SqlNameHit>()
        for (regex in listOf(afterKeyword, qualifiedCall)) {
            for (match in regex.findAll(text)) {
                val group = match.groups[1] ?: continue
                val (schema, name) = normalize(group.value)
                if (name in reserved || name.startsWith("@") || name.startsWith("#")) continue
                hits.putIfAbsent(group.range.first, SqlNameHit(name, schema, group.range.first, group.value.length))
            }
        }
        return hits.values.toList()
    }
}
