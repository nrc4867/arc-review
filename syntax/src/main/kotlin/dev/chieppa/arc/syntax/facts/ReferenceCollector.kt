package dev.chieppa.arc.syntax.facts

import dev.chieppa.arc.syntax.grammar.LanguageFamily
import dev.chieppa.arc.syntax.runtime.QueryKind
import dev.chieppa.arc.syntax.runtime.SyntaxQueries
import dev.chieppa.arc.syntax.runtime.SyntaxTree

/**
 * Reads a tree's references: every identifier the names query finds outside a definition's name, typed as a call or a
 * type where the references query says so (with a call's argument count and a SQL name's schema), plus the SQL names
 * found in string literals.
 */
internal object ReferenceCollector {
    private const val NAME = "name"
    private const val ARG = "arg"
    private const val ARGS = "args"
    private const val SCHEMA = "schema"
    private const val CALL = "reference.call"
    private const val TYPE = "reference.type"

    /** What the references query says about the name starting at one index. */
    private class Typing(var kind: RefKind, var args: Int, var schema: String?)

    fun collect(tree: SyntaxTree, lines: LineIndex, definitions: List<Definition>, sqlNames: List<SqlNameHit>): List<Reference> {
        val text = tree.text
        val sql = tree.grammar.family == LanguageFamily.Sql
        val typing = typings(tree)
        val defNames = definitions.map { it.nameStart until it.nameEnd }.sortedBy { it.first }
        val references = ArrayList<Reference>()
        SyntaxQueries.of(tree.grammar, QueryKind.Names)?.let { query ->
            val records = query.run(tree)
            for (i in 0 until records.size) {
                val start = records.start(i)
                val end = records.end(i)
                if (end <= start || insideAny(defNames, start)) continue
                val raw = text.substring(start, end)
                val name = (if (sql) SqlNames.normalize(raw).second else raw).intern()
                val typed = typing[start]
                references += Reference(
                    name = name,
                    kind = typed?.kind ?: RefKind.Name,
                    start = start,
                    end = end,
                    line = lines.line(start),
                    column = lines.column(start),
                    arity = typed?.args?.takeIf { it >= 0 },
                    qualified = CodeText.followsDot(text, start),
                    schema = typed?.schema,
                )
            }
        }
        for (hit in sqlNames) {
            references += Reference(
                hit.name.intern(), RefKind.SqlString, hit.offset, hit.offset + hit.length, lines.line(hit.offset), lines.column(hit.offset), schema = hit.schema,
            )
        }
        return references.distinctBy { it.start }.sortedBy { it.start }
    }

    private fun typings(tree: SyntaxTree): Map<Int, Typing> {
        val query = SyntaxQueries.of(tree.grammar, QueryKind.References) ?: return emptyMap()
        val names = query.captureNames
        val typing = HashMap<Int, Typing>()
        val lists = ListMembers()
        val records = query.run(tree)
        records.forEachMatch { from, until ->
            var nameStart = -1
            var kind = RefKind.Name
            var args = -1
            var schema: String? = null
            for (i in from until until) {
                when (names[records.capture(i)]) {
                    NAME -> nameStart = records.start(i)
                    ARG -> lists.addMember(records.start(i), "")
                    ARGS -> {
                        args = records.start(i)
                        lists.addList(records.start(i), records.end(i))
                    }
                    SCHEMA -> schema = SqlNames.normalize(tree.text.substring(records.start(i), records.end(i))).second
                    CALL -> kind = RefKind.Call
                    TYPE -> kind = RefKind.Type
                }
            }
            if (nameStart < 0) return@forEachMatch
            val existing = typing[nameStart]
            if (existing == null) {
                typing[nameStart] = Typing(kind, args, schema)
            } else {
                if (kind == RefKind.Call && existing.kind != RefKind.Call) {
                    existing.kind = RefKind.Call
                    existing.args = args
                }
                if (existing.schema == null) existing.schema = schema
            }
        }
        val counts = lists.byList()
        for (typed in typing.values) typed.args = if (typed.kind == RefKind.Call && typed.args >= 0) counts[typed.args]?.size ?: 0 else -1
        return typing
    }

    private fun insideAny(ranges: List<IntRange>, offset: Int): Boolean {
        var low = 0
        var high = ranges.size - 1
        while (low <= high) {
            val mid = (low + high) ushr 1
            val range = ranges[mid]
            when {
                offset < range.first -> high = mid - 1
                offset > range.last -> low = mid + 1
                else -> return true
            }
        }
        return false
    }
}
