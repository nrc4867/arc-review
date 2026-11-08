package dev.chieppa.arc.syntax.runtime

import dev.chieppa.arc.syntax.grammar.Grammar
import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout

/**
 * A compiled tree-sitter query for one grammar. Compiled queries are immutable and shared by every thread; they live
 * as long as the process. [run] executes it natively in one call and applies its text predicates.
 */
class SyntaxQuery private constructor(val grammar: Grammar, private val pointer: MemorySegment) {
    /** The query's capture names, by capture index. */
    val captureNames: List<String>

    private val predicates: List<List<QueryPredicate>>
    private val hasPredicates: Boolean

    init {
        val captures = TreeSitterApi.queryCaptureCount.invokeWithArguments(pointer) as Int
        captureNames = (0 until captures).map { NativeText.captureName(pointer, it) }
        predicates = QueryPredicates.read(pointer, TreeSitterApi.queryPatternCount.invokeWithArguments(pointer) as Int)
        hasPredicates = predicates.any { it.isNotEmpty() }
    }

    /** The index of capture [name], or -1 when the query has none by that name. */
    fun captureIndex(name: String): Int = captureNames.indexOf(name)

    /** Every capture of every match in [tree] that passes the query's predicates. */
    fun run(tree: SyntaxTree): QueryRecords {
        val records = runNative(tree)
        if (!hasPredicates) return records
        return records.filterMatches { from, until ->
            val tests = predicates[records.pattern(from)]
            tests.isEmpty() || tests.all { predicate ->
                predicate.test { capture ->
                    (from until until).filter { records.capture(it) == capture }.map { tree.text.substring(records.start(it), records.end(it)) }
                }
            }
        }
    }

    private fun runNative(tree: SyntaxTree): QueryRecords {
        var capacity = INITIAL_RECORDS
        Arena.ofConfined().use { arena ->
            while (true) {
                val buffer = arena.allocate(ValueLayout.JAVA_INT, QueryRecords.STRIDE.toLong() * capacity)
                val count = TreeSitterApi.queryRecords.invokeWithArguments(tree.pointer, pointer, buffer, capacity) as Int
                if (count <= capacity) {
                    return QueryRecords(buffer.asSlice(0, QueryRecords.STRIDE.toLong() * Int.SIZE_BYTES * count).toArray(ValueLayout.JAVA_INT))
                }
                capacity = count
            }
        }
    }

    companion object {
        private const val INITIAL_RECORDS = 1 shl 14

        /** Compiles [source] for [grammar]; throws [QueryException] naming where it fails. */
        fun compile(grammar: Grammar, source: String): SyntaxQuery = Arena.ofConfined().use { arena ->
            val bytes = source.toByteArray(Charsets.UTF_8)
            val text = arena.allocate(bytes.size.toLong() + 1)
            MemorySegment.copy(bytes, 0, text, ValueLayout.JAVA_BYTE, 0, bytes.size)
            val offset = arena.allocate(ValueLayout.JAVA_INT)
            val type = arena.allocate(ValueLayout.JAVA_INT)
            val pointer = TreeSitterApi.queryNew.invokeWithArguments(SyntaxLanguages.of(grammar), text, bytes.size, offset, type) as MemorySegment
            if (pointer == MemorySegment.NULL) {
                val at = offset.get(ValueLayout.JAVA_INT, 0)
                val line = String(bytes, 0, at.coerceAtMost(bytes.size), Charsets.UTF_8).count { it == '\n' } + 1
                throw QueryException("${grammar.id} query error ${type.get(ValueLayout.JAVA_INT, 0)} at line $line: ${String(bytes, at.coerceAtMost(bytes.size), minOf(SNIPPET, bytes.size - at), Charsets.UTF_8)}")
            }
            SyntaxQuery(grammar, pointer)
        }

        private const val SNIPPET = 60
    }
}

/** A query that does not compile against its grammar. */
class QueryException(message: String) : RuntimeException(message)
