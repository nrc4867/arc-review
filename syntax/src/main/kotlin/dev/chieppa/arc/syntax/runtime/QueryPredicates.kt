package dev.chieppa.arc.syntax.runtime

import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout

/** Reads a compiled query's predicates, pattern by pattern, into [QueryPredicate]s. */
internal object QueryPredicates {
    private const val STEP_BYTES = 8L
    private const val DONE = 0
    private const val CAPTURE = 1
    private const val ANY_OF = "any-of?"
    private const val NOT_ANY_OF = "not-any-of?"

    /** One step of a predicate: a capture index or a string. */
    private data class Step(val capture: Int?, val string: String?)

    fun read(query: MemorySegment, patterns: Int): List<List<QueryPredicate>> = Arena.ofConfined().use { arena ->
        val count = arena.allocate(ValueLayout.JAVA_INT)
        (0 until patterns).map { pattern ->
            val steps = TreeSitterApi.queryPredicates.invokeWithArguments(query, pattern, count) as MemorySegment
            val n = count.get(ValueLayout.JAVA_INT, 0)
            if (n == 0) return@map emptyList()
            val array = steps.reinterpret(n * STEP_BYTES)
            val predicates = ArrayList<QueryPredicate>()
            var current = ArrayList<Step>()
            for (i in 0 until n) {
                val type = array.get(ValueLayout.JAVA_INT, i * STEP_BYTES)
                val id = array.get(ValueLayout.JAVA_INT, i * STEP_BYTES + Int.SIZE_BYTES)
                when (type) {
                    DONE -> {
                        parse(current)?.let(predicates::add)
                        current = ArrayList()
                    }
                    CAPTURE -> current += Step(id, null)
                    else -> current += Step(null, NativeText.queryString(query, id))
                }
            }
            predicates
        }
    }

    private fun parse(steps: List<Step>): QueryPredicate? {
        val name = steps.firstOrNull()?.string ?: return null
        val capture = steps.getOrNull(1)?.capture ?: return null
        val rest = steps.drop(2)
        if (name == ANY_OF || name == NOT_ANY_OF) return QueryPredicate.AnyOf(capture, rest.mapNotNull { it.string }.toSet(), name == NOT_ANY_OF)
        val negate = name.removePrefix("any-").startsWith("not-")
        val any = name.startsWith("any-")
        return when (name.removePrefix("any-").removePrefix("not-")) {
            "eq?" -> QueryPredicate.Equals(capture, rest.firstOrNull()?.capture, rest.firstOrNull()?.string, negate, any)
            "match?" -> rest.firstOrNull()?.string?.let { QueryPredicate.Matches(capture, Regex(it), negate, any) }
            "lua-match?" -> rest.firstOrNull()?.string?.let { QueryPredicate.Matches(capture, Regex(LuaPatterns.toRegex(it)), negate, any) }
            else -> null
        }
    }
}
