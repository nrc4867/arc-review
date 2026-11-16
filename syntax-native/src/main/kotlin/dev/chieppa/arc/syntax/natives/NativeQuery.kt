package dev.chieppa.arc.syntax.natives

import dev.chieppa.arc.syntax.engine.EngineQuery
import dev.chieppa.arc.syntax.engine.PredicateStep
import dev.chieppa.arc.syntax.engine.QueryErrors
import dev.chieppa.arc.syntax.engine.QueryException
import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout

/** A compiled query in native memory, kept for the life of the process and shared by every thread. */
internal class NativeQuery private constructor(val grammar: String, val pointer: MemorySegment) : EngineQuery {
    override val captureNames: List<String> =
        (0 until TreeSitterApi.queryCaptureCount.invokeWithArguments(pointer) as Int).map { NativeText.captureName(pointer, it) }

    override val patternCount: Int = TreeSitterApi.queryPatternCount.invokeWithArguments(pointer) as Int

    private val steps: List<List<PredicateStep>> = Arena.ofConfined().use { arena ->
        val count = arena.allocate(ValueLayout.JAVA_INT)
        (0 until patternCount).map { pattern ->
            val array = TreeSitterApi.queryPredicates.invokeWithArguments(pointer, pattern, count) as MemorySegment
            val n = count.get(ValueLayout.JAVA_INT, 0)
            val steps = array.reinterpret(n * STEP_BYTES)
            (0 until n).map { i ->
                val type = steps.get(ValueLayout.JAVA_INT, i * STEP_BYTES)
                val id = steps.get(ValueLayout.JAVA_INT, i * STEP_BYTES + Int.SIZE_BYTES)
                when (type) {
                    PredicateStep.TYPE_DONE -> PredicateStep.Done
                    PredicateStep.TYPE_CAPTURE -> PredicateStep.Capture(id)
                    else -> PredicateStep.Text(NativeText.queryString(pointer, id))
                }
            }
        }
    }

    override fun predicates(pattern: Int): List<PredicateStep> = steps[pattern]

    companion object {
        private const val STEP_BYTES = 8L

        /** Compiles [source] for [grammar]; throws [QueryException] naming where it fails. */
        fun compile(grammar: String, source: String): NativeQuery = Arena.ofConfined().use { arena ->
            val bytes = source.toByteArray(Charsets.UTF_8)
            val text = arena.allocate(bytes.size.toLong() + 1)
            MemorySegment.copy(bytes, 0, text, ValueLayout.JAVA_BYTE, 0, bytes.size)
            val offset = arena.allocate(ValueLayout.JAVA_INT)
            val type = arena.allocate(ValueLayout.JAVA_INT)
            val pointer = TreeSitterApi.queryNew.invokeWithArguments(SyntaxLanguages.of(grammar), text, bytes.size, offset, type) as MemorySegment
            if (pointer == MemorySegment.NULL) {
                throw QueryException(QueryErrors.message(grammar, bytes, offset.get(ValueLayout.JAVA_INT, 0), type.get(ValueLayout.JAVA_INT, 0)))
            }
            NativeQuery(grammar, pointer)
        }
    }
}
