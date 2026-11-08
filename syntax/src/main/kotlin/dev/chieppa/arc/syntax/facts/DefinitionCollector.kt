package dev.chieppa.arc.syntax.facts

import dev.chieppa.arc.syntax.grammar.LanguageFamily
import dev.chieppa.arc.syntax.runtime.QueryKind
import dev.chieppa.arc.syntax.runtime.SyntaxQueries
import dev.chieppa.arc.syntax.runtime.SyntaxTree

/**
 * Reads a tree's definitions from its grammar's definitions query. One definition can match several patterns (one
 * giving its parameters, another its type); they merge by name position, the more telling kind winning. Each
 * definition then learns the innermost definition around it.
 */
internal object DefinitionCollector {
    private const val DEFINITION = "definition."
    private const val NAME = "name"
    private const val PARAM = "param"
    private const val PARAMS = "params"
    private const val TYPE = "type"

    private class Draft(
        var kind: SymbolKind,
        val nameStart: Int,
        val nameEnd: Int,
        var spanStart: Int,
        var spanEnd: Int,
        var params: Int,
        var type: String?,
    )

    fun collect(tree: SyntaxTree, lines: LineIndex): List<Definition> {
        val query = SyntaxQueries.of(tree.grammar, QueryKind.Definitions) ?: return emptyList()
        val names = query.captureNames
        val text = tree.text
        val drafts = LinkedHashMap<Int, Draft>()
        val lists = ListMembers()
        val records = query.run(tree)
        records.forEachMatch { from, until ->
            var kind: SymbolKind? = null
            var spanStart = -1
            var spanEnd = -1
            var nameStart = -1
            var nameEnd = -1
            var type: String? = null
            var params = -1
            for (i in from until until) {
                val capture = names[records.capture(i)]
                when {
                    capture.startsWith(DEFINITION) -> {
                        kind = SymbolKind.of(capture.removePrefix(DEFINITION))
                        spanStart = records.start(i)
                        spanEnd = records.end(i)
                    }
                    capture == NAME -> { nameStart = records.start(i); nameEnd = records.end(i) }
                    capture == PARAM -> lists.addMember(records.start(i), CodeText.compact(text.substring(records.start(i), records.end(i)).substringBefore('=')))
                    capture == PARAMS -> {
                        params = records.start(i)
                        lists.addList(records.start(i), records.end(i))
                    }
                    capture == TYPE -> type = CodeText.compact(text.substring(records.start(i), records.end(i)))
                }
            }
            val found = kind ?: return@forEachMatch
            if (nameStart < 0 || nameEnd <= nameStart) return@forEachMatch
            val existing = drafts[nameStart]
            if (existing == null) {
                drafts[nameStart] = Draft(found, nameStart, nameEnd, spanStart, spanEnd, params, type)
            } else {
                if (found.rank > existing.kind.rank) existing.kind = found
                if (existing.type == null) existing.type = type
                if (existing.params < 0) existing.params = params
                if (spanEnd - spanStart > existing.spanEnd - existing.spanStart) {
                    existing.spanStart = spanStart
                    existing.spanEnd = spanEnd
                }
            }
        }
        val parameterLists = lists.byList()
        val sql = tree.grammar.family == LanguageFamily.Sql
        val ordered = drafts.values.sortedWith(compareBy<Draft> { it.spanStart }.thenByDescending { it.spanEnd })
        val built = ordered.map { draft ->
            val label = text.substring(draft.nameStart, draft.nameEnd)
            val (schema, sqlName) = if (sql) SqlNames.normalize(label) else (null to label)
            Definition(
                name = sqlName.intern(),
                label = if (sql) label.substringAfterLast('.').trim().removeSurrounding("[", "]") else label,
                kind = draft.kind,
                nameStart = draft.nameStart,
                nameEnd = draft.nameEnd,
                line = lines.line(draft.nameStart),
                column = lines.column(draft.nameStart),
                spanStart = draft.spanStart,
                spanEnd = draft.spanEnd,
                parameters = if (draft.kind.isCallable) parameterLists[draft.params].orEmpty() else null,
                type = draft.type,
                container = schema,
            )
        }
        return withParents(built)
    }

    /** Gives each definition the innermost definition whose span holds its name; [defs] are in span order. */
    private fun withParents(defs: List<Definition>): List<Definition> {
        val open = ArrayDeque<Int>()
        return defs.mapIndexed { index, def ->
            while (open.isNotEmpty() && defs[open.last()].spanEnd <= def.nameStart) open.removeLast()
            val parent = open.lastOrNull { defs[it].spanStart <= def.nameStart && def.nameStart < defs[it].spanEnd }
            open.addLast(index)
            if (parent == null) {
                def
            } else {
                val outer = defs[parent]
                def.copy(parent = parent, container = def.container ?: outer.name, containerKind = outer.kind)
            }
        }
    }
}
