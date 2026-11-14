package dev.chieppa.arc.host.github.api

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/**
 * Names a GraphQL request by what it asks for, so the activity log tells one `POST /graphql` from another: `mutation`
 * for a mutation, then the top-level fields of its selection, as `search, repository` or `mutation addReaction`.
 */
object GraphqlLabel {
    private val json = Json { ignoreUnknownKeys = true }
    private val NAME = Regex("[_A-Za-z][_0-9A-Za-z]*")
    private const val MUTATION = "mutation"
    private const val QUERY = "query"
    private const val MAX_FIELDS = 4

    /** The label of the request whose JSON [body] holds the `query`, or null when it holds none. */
    fun of(body: String?): String? {
        val root = body?.let { runCatching { json.parseToJsonElement(it) }.getOrNull() } as? JsonObject ?: return null
        val query = (root[QUERY] as? JsonPrimitive)?.takeIf { it.isString }?.content ?: return null
        val open = query.indexOf('{').takeIf { it >= 0 } ?: return null
        val fields = topFields(query, open)
        if (fields.isEmpty()) return null
        val shown = fields.take(MAX_FIELDS).joinToString(", ") + if (fields.size > MAX_FIELDS) ", …" else ""
        return if (query.substring(0, open).trimStart().startsWith(MUTATION)) "$MUTATION $shown" else shown
    }

    /** The fields at the first level of the selection opening at [open], aliases dropped, each named once. */
    private fun topFields(query: String, open: Int): List<String> {
        val fields = linkedSetOf<String>()
        var depth = 0
        var parens = 0
        var i = open
        while (i < query.length) {
            val c = query[i]
            when {
                c == '"' -> i = skipString(query, i)
                c == '#' -> i = query.indexOf('\n', i).let { if (it < 0) query.length else it }
                c == '(' -> parens++
                c == ')' -> parens--
                c == '{' && parens == 0 -> depth++
                c == '}' && parens == 0 -> if (--depth == 0) return fields.toList()
                depth == 1 && parens == 0 && (c == '_' || c.isLetter()) -> {
                    val name = NAME.find(query, i)!!.value
                    val after = query.substring(i + name.length).trimStart()
                    if (!after.startsWith(":") && name != "on" && name != "fragment") fields += name
                    i += name.length - 1
                }
            }
            i++
        }
        return fields.toList()
    }

    private fun skipString(query: String, start: Int): Int {
        var i = start + 1
        while (i < query.length && query[i] != '"') i += if (query[i] == '\\') 2 else 1
        return i
    }
}
