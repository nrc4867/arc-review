package dev.chieppa.arc.syntax.wasm

import com.dylibso.chicory.runtime.ExportFunction
import com.dylibso.chicory.runtime.Instance
import dev.chieppa.arc.syntax.engine.CaptureLayout
import dev.chieppa.arc.syntax.engine.PredicateStep
import dev.chieppa.arc.syntax.engine.QueryErrors
import dev.chieppa.arc.syntax.engine.QueryException
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * One instance of a grammar's module with a parser set to its language: tree-sitter's C API over the instance's
 * memory. Not thread-safe; [GrammarPool] lends each to one parse at a time. Queries are compiled into it by index,
 * the index [GrammarPool] gives each query of the grammar; a query whose source the build compiled into the
 * grammar's [Snapshot] is already in memory.
 */
internal class WasmRuntime private constructor(
    val grammar: WasmGrammar,
    val instance: Instance,
    val state: RuntimeState,
) {
    private val memory = instance.memory()
    private val malloc = export("malloc")
    private val free = export("free")
    private val parse = export("ts_parser_parse_string_encoding")
    private val deleteTree = export("ts_tree_delete")
    private val records = export("arc_query_records")

    private var buffer = 0L
    private var capacity = 0
    private val queries = ArrayList<Long>()

    /** How many bytes of memory the instance holds now. */
    val memoryBytes: Long get() = memory.pages().toLong() * WasmLimits.PAGE_BYTES

    /** Parses [text]; gives the tree's address in this instance. */
    fun parse(text: String): Long {
        val bytes = text.toByteArray(Charsets.UTF_16LE)
        val input = call(malloc, maxOf(bytes.size, 1).toLong())
        memory.write(input.toInt(), bytes)
        val tree = call(parse, state.parser, 0, input, bytes.size.toLong(), WasmLimits.ENCODING_UTF16LE)
        free.apply(input)
        check(tree != 0L) { "tree-sitter gave no tree for ${grammar.id}" }
        return tree
    }

    fun deleteTree(tree: Long) {
        deleteTree.apply(tree)
    }

    /** Every capture of query [index] over [tree], compiling it from [source] first when this instance lacks it. */
    fun captures(tree: Long, index: Int, source: () -> String): IntArray {
        while (queries.size <= index) queries += 0L
        if (queries[index] == 0L) queries[index] = query(source())
        val query = queries[index]
        if (buffer == 0L) grow(WasmLimits.INITIAL_RECORDS)
        var count = call(records, tree, query, buffer, capacity.toLong()).toInt()
        if (count > capacity) {
            grow(count)
            count = call(records, tree, query, buffer, capacity.toLong()).toInt()
        }
        val bytes = memory.readBytes(buffer.toInt(), count * CaptureLayout.BYTES)
        val out = IntArray(count * CaptureLayout.STRIDE)
        ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asIntBuffer().get(out)
        return out
    }

    /** Takes [source] as query [index], giving its capture names, pattern count and predicate steps. */
    fun describe(index: Int, source: String): QueryShape {
        while (queries.size <= index) queries += 0L
        val query = query(source).also { queries[index] = it }
        val captures = call(export("ts_query_capture_count"), query).toInt()
        val patterns = call(export("ts_query_pattern_count"), query).toInt()
        val names = (0 until captures).map { text(export("ts_query_capture_name_for_id"), query, it) }
        return QueryShape(names, (0 until patterns).map { predicates(query, it) })
    }

    /** The address of query [source]: the snapshot's when the build compiled it, else compiled now. */
    private fun query(source: String): Long = state.prebuilt[QueryHash.of(source)] ?: compile(source)

    /** Compiles [source] into this instance; throws [QueryException] naming where it fails. */
    fun compile(source: String): Long {
        val bytes = source.toByteArray(Charsets.UTF_8)
        val text = call(malloc, bytes.size + 1L)
        memory.write(text.toInt(), bytes)
        memory.writeByte(text.toInt() + bytes.size, 0.toByte())
        val scratch = state.scratch
        val query = call(export("ts_query_new"), state.language, text, bytes.size.toLong(), scratch, scratch + Int.SIZE_BYTES)
        free.apply(text)
        if (query == 0L) {
            val offset = memory.readInt(scratch.toInt())
            val type = memory.readInt(scratch.toInt() + Int.SIZE_BYTES)
            throw QueryException(QueryErrors.message(grammar.id, bytes, offset, type))
        }
        return query
    }

    private fun predicates(query: Long, pattern: Int): List<PredicateStep> {
        val steps = call(export("ts_query_predicates_for_pattern"), query, pattern.toLong(), state.scratch).toInt()
        val count = memory.readInt(state.scratch.toInt())
        return (0 until count).map { i ->
            val type = memory.readInt(steps + i * STEP_BYTES)
            val id = memory.readInt(steps + i * STEP_BYTES + Int.SIZE_BYTES)
            when (type) {
                PredicateStep.TYPE_DONE -> PredicateStep.Done
                PredicateStep.TYPE_CAPTURE -> PredicateStep.Capture(id)
                else -> PredicateStep.Text(text(export("ts_query_string_value_for_id"), query, id))
            }
        }
    }

    private fun text(function: ExportFunction, query: Long, id: Int): String {
        val pointer = call(function, query, id.toLong(), state.scratch).toInt()
        val length = memory.readInt(state.scratch.toInt())
        return String(memory.readBytes(pointer, length), Charsets.UTF_8)
    }

    private fun grow(records: Int) {
        if (buffer != 0L) free.apply(buffer)
        capacity = records
        buffer = call(malloc, records.toLong() * CaptureLayout.BYTES)
    }

    private fun export(name: String): ExportFunction = instance.export(name)

    /** A compiled query's capture names and each pattern's predicate steps. */
    class QueryShape(val captureNames: List<String>, val predicates: List<List<PredicateStep>>)

    companion object {
        private const val STEP_BYTES = 2 * Int.SIZE_BYTES

        private fun call(function: ExportFunction, vararg args: Long): Long = function.apply(*args)[0]

        /** A new instance of [grammar], initialised, with its parser and language and no queries. */
        fun start(grammar: WasmGrammar): WasmRuntime {
            val instance = grammar.instantiate()
            instance.export("_initialize").apply()
            val language = call(instance.export(grammar.entry))
            val parser = call(instance.export("ts_parser_new"))
            check(call(instance.export("ts_parser_set_language"), parser, language) != 0L) {
                "tree-sitter rejected the ${grammar.id} language's version"
            }
            val scratch = call(instance.export("malloc"), 2L * Int.SIZE_BYTES)
            return WasmRuntime(grammar, instance, RuntimeState(language, parser, scratch, emptyMap()))
        }

        /** A new instance of [grammar] as the build left it in [snapshot], with Arc's queries compiled. */
        fun restore(grammar: WasmGrammar, snapshot: Snapshot): WasmRuntime {
            val instance = grammar.instantiate()
            snapshot.applyTo(instance)
            return WasmRuntime(grammar, instance, snapshot.state)
        }

        /** A new instance of [template]'s grammar holding a copy of its memory and globals, so its queries too. */
        fun copyOf(template: WasmRuntime): WasmRuntime {
            val instance = template.grammar.instantiate()
            InstanceCopy.copy(template.instance, instance)
            return WasmRuntime(template.grammar, instance, template.state).apply {
                buffer = template.buffer
                capacity = template.capacity
                queries += template.queries
            }
        }
    }
}
